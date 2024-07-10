/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.fragments;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxrelay2.PublishRelay;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import sdk.chat.ui.Contact;
import sdk.chat.ui.ContactsAdapter;
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
    protected Set<String> registeredUsers = new HashSet<>(); // default
    private ContactsAdapter adapter1;

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

        LoaderManager.getInstance(this).initLoader(0, null, this);

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

        if (dm != null) {
            dm.dispose();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveRecyclerViewState();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(true);
        restoreRecyclerViewState();
    }

    private void saveRecyclerViewState() {
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

    private void restoreRecyclerViewState() {
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
        List<Contact> filteredContacts = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredContacts.add(contact);
            }
        }

        //adapter1 = new ContactRecyclerViewAdapter(getActivity(), filteredContacts, registeredUsers);
        adapter1 = new ContactsAdapter(getActivity(), filteredContacts, registeredUsers);

        if (recyclerView != null) {
            recyclerView.setAdapter(adapter1);
        } else {
            Log.e("ContactsFragment", "RecyclerView is null");
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
        return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Set<String> addedContacts = new HashSet<>();
        registeredUsers = RegisteredUserService.listRegisteredUsers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                String validPhoneNumber = validPhoneNumber(phoneNumber);

                if (!addedContacts.contains(validPhoneNumber)) {
                    Contact contact = new Contact(name, phoneNumber, photoUri);
                    contacts.add(contact);
                    addedContacts.add(validPhoneNumber);
                }
            } while (cursor.moveToNext());
        }

        loadAdapter();
        loadAddContactList();
    }

    private void loadAddContactList() {
        for (Contact contact : registeredContacts) {
            dm.add(ChatSDK.core().getUserForEntityID(validPhoneNumber(contact.getNumber())).flatMapCompletable(user -> {
                user.setAvatarURL(contact.getPhoto());
                String name = contact.getName();
                user.setName(name);
                user.setPhoneNumber(contact.getNumber());

                return ChatSDK.contact().addContact(user, ConnectionType.Contact);
            }).subscribe(() -> {
                Logger.debug("Success");
            }, error -> {
                Logger.debug("Error: " + error.getMessage());
            }));
        }
    }

    protected void loadAdapter() {
        adapter1 = new ContactsAdapter(getActivity(), contacts, registeredUsers);

        if (recyclerView != null) {
            recyclerView.setAdapter(adapter1);
        } else {
            Log.e("ContactsFragment", "RecyclerView is null");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
