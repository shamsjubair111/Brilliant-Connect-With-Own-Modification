/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.fragments;

import static sdk.chat.ui.ContactUtils.contactArrayList;
import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.jakewharton.rxrelay2.PublishRelay;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.types.SearchActivityType;
import sdk.chat.core.utils.UserListItemConverter;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.Constants;
import sdk.chat.ui.Contact;
import sdk.chat.ui.ContactRecyclerViewAdapter;
import sdk.chat.ui.R;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.api.RegisteredUserService;
import sdk.chat.ui.interfaces.SearchSupported;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.chat.ui.utils.DialogUtils;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment implements SearchSupported, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 123;
    public ContactRecyclerViewAdapter adapter1;
    protected UsersListAdapter adapter;
    protected PublishRelay<User> onClickRelay = PublishRelay.create();
    protected PublishRelay<User> onLongClickRelay = PublishRelay.create();
    protected Disposable listOnClickListenerDisposable;
    protected Disposable listOnLongClickListenerDisposable;
    protected String filter;
    protected List<User> sourceUsers = new ArrayList<>();
    protected RecyclerView recyclerView;
    protected ConstraintLayout root;
    protected Map<Long, List<String>> phones = new HashMap<>();
    protected List<Contact> contacts = new ArrayList<>();
    protected Set<Contact> registeredContacts = new HashSet<>();
    Set<String> registeredUsers = new HashSet<>();

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        root = view.findViewById(R.id.root);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            if (contactArrayList == null) {
                LoaderManager.getInstance(this).initLoader(0, null, this);
            }
        }
        initViews();

        //loadData(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        addListeners();

    }

    @Override
    public void onStop() {
        super.onStop();

        dm.dispose();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save the scroll position of the RecyclerView
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int index = layoutManager.findFirstVisibleItemPosition();
            View v = recyclerView.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());

            SharedPreferences prefs = getActivity().getSharedPreferences("ContactsPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("index", index);
            editor.putInt("top", top);
            editor.apply();
        }
    }

    public void addListeners() {

        if (listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User clickedUser = (User) o;

                onClickRelay.accept(clickedUser);
                startProfileActivity(clickedUser.getEntityID());
            }
        });

        if (listOnLongClickListenerDisposable != null) {
            listOnLongClickListenerDisposable.dispose();
        }
        listOnLongClickListenerDisposable = adapter.onLongClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User user = (User) o;
                onLongClickRelay.accept(user);

                DialogUtils.showToastDialog(getContext(), R.string.delete_contact, 0, R.string.delete, R.string.cancel, () -> {
                    ChatSDK.contact().deleteContact(user, ConnectionType.Contact).subscribe(ContactsFragment.this);
                }, null);
            }
        });

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterContactsChanged()).subscribe(networkEvent -> loadData(true)));

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserPresenceUpdated)).subscribe(networkEvent -> loadData(true)));

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).subscribe(networkEvent -> loadData(true)));
    }

    public void startProfileActivity(String userEntityID) {
        ChatSDK.ui().startProfileActivity(getContext(), userEntityID);
    }

    public void initViews() {

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new UsersListAdapter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ChatSDKUI.provider().menuItems().addAddItem(getContext(), menu, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        // Each user that will be found in the filter context will be automatically added as a contact.
        if (id == MenuItemProvider.addItemId) {
            startSearchActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startSearchActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final List<SearchActivityType> activities = new ArrayList<>(ChatSDK.ui().getSearchActivities());

        if (activities.size() == 1) {
            activities.get(0).startFrom(getActivity());
            return;
        }

        String[] items = new String[activities.size()];
        int i = 0;

        for (SearchActivityType activity : activities) {
            items[i++] = activity.title;
        }

        builder.setTitle(getActivity().getString(R.string.search)).setItems(items, (dialogInterface, index) -> {
            // Launch the appropriate context
            activities.get(index).startFrom(getActivity());
        });

        builder.show();
    }

    public void loadData(final boolean force) {
        dm.add(Single.create((SingleOnSubscribe<Optional<List<UserListItem>>>) emitter -> {
            final ArrayList<User> originalUserList = new ArrayList<>(sourceUsers);
            reloadData();
            if (!originalUserList.equals(sourceUsers) || force) {
                emitter.onSuccess(new Optional<>(UserListItemConverter.toUserItemList(sourceUsers)));
            } else {
                emitter.onSuccess(new Optional<>());
            }
        }).subscribeOn(RX.db()).observeOn(RX.main()).subscribe(listOptional -> {
            if (!listOptional.isEmpty()) {
                adapter.setUsers(listOptional.get(), true);
            }
        }));
    }

    @Override
    public void clearData() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(true);

        // Restore the scroll position of the RecyclerView
        SharedPreferences prefs = getActivity().getSharedPreferences("ContactsPrefs", Context.MODE_PRIVATE);
        int index = prefs.getInt("index", -1);
        int top = prefs.getInt("top", 0);

        if (index != -1) {
            recyclerView.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(index, top);
                }
            });
        }
    }

    @Override
    public void reloadData() {
        sourceUsers.clear();
        sourceUsers.addAll(filter(ChatSDK.contact().contacts()));
    }

    public Observable<User> onClickObservable() {
        return onClickRelay;
    }

    public Observable<User> onLongClickObservable() {
        return onLongClickRelay;
    }

    @Override

    public void filter(String text) {
        filter = text;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<Contact> filteredContacts = contacts.stream()
                    .filter(contact -> contact.getName().toLowerCase().startsWith(text.toLowerCase()))
                    .collect(Collectors.toList());

            adapter1 = new ContactRecyclerViewAdapter(getActivity(), filteredContacts, registeredUsers);


            if (filteredContacts != null) {
                recyclerView.setAdapter(adapter1);
            }
        }



//        loadData(false);
    }


    public List<User> filter(List<User> users) {
        if (filter == null || filter.isEmpty()) {
            return users;
        }

        List<User> filteredUsers = new ArrayList<>();
        for (User u : users) {
            if (u.getName() != null && u.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredUsers.add(u);
            }
        }
        return filteredUsers;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case 0:
                return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, Constants.PROJECTION_NUMBERS, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC");
            default:
                return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, Constants.PROJECTION_DETAILS, null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0:
                phones = new HashMap<>();
                if (data != null) {
                    while (!data.isClosed() && data.moveToNext()) {
                        long contactId = data.getLong(0);
                        String phone = data.getString(1);
                        List<String> list;
                        if (phones.containsKey(contactId)) {
                            list = phones.get(contactId);
                        } else {
                            list = new ArrayList<>();
                            phones.put(contactId, list);
                        }
                        list.add(phone);
                    }
                    data.close();
                }
                LoaderManager.getInstance(this).initLoader(1, null, this);
                break;
            case 1:
                if (data != null) {
                    registeredUsers = RegisteredUserService.listRegisteredUsers();
                    Set<String> addedContactIds = new HashSet<>();
                    while (!data.isClosed() && data.moveToNext()) {
                        long contactId = data.getLong(0);
                        String name = data.getString(1);
                        String photo = data.getString(2);
                        List<String> contactPhones = phones.get(contactId);
                        if (contactPhones != null) {
                            for (String phone : contactPhones) {
                                try {
                                    String validPhoneNumber = validPhoneNumber(phone);
                                    if (phone != null && !addedContactIds.contains(validPhoneNumber)) {
                                        Contact e = new Contact(contactId, name, phone, photo);
                                        contacts.add(e);
                                        addedContactIds.add(validPhoneNumber);
                                        if (registeredUsers.contains(validPhoneNumber)) {
                                            registeredContacts.add(e);
                                        }
                                    }
                                } catch (NumberParseException e) {
//                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    data.close();
                    loadAdapter();
                    loadAddContactList();
                }
        }
    }

    private void loadAddContactList() {
        try {
            for (Contact contact : registeredContacts) {
                dm.add(ChatSDK.core()
                    .getUserForEntityID(validPhoneNumber(contact.getNumber()))
                    .flatMapCompletable(user -> {
                        user.setAvatarURL(contact.getPhoto());
                        String name = contact.getName();
                        user.setName(name);
                        user.setPhoneNumber(contact.getNumber());


                        return ChatSDK.contact().addContact(user, ConnectionType.Contact);
                    })
                    .subscribe(() -> {
                        Logger.debug("Success");
                    }, error -> {
                        Logger.debug("Error: " + error.getMessage());
                    }));
            }
        } catch (NumberParseException e) {
            throw new RuntimeException(e);
        }


    }

    protected void loadAdapter() {
        adapter1 = new ContactRecyclerViewAdapter(getContext(), contacts, registeredUsers);

        if (contacts != null) {
            recyclerView.setAdapter(adapter1);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
