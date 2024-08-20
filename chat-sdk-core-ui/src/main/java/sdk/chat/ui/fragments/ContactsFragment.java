package sdk.chat.ui.fragments;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.ui.Contact;
import sdk.chat.ui.ContactsAdapter;
import sdk.chat.ui.R;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.api.RegisteredUserService;
import sdk.chat.ui.interfaces.SearchSupported;

public class ContactsFragment extends BaseFragment implements SearchSupported, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 123;
    protected UsersListAdapter adapter;
    protected String filter;
    protected RecyclerView recyclerView;
    protected ConstraintLayout root;
    protected List<Contact> contacts;
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
        registeredUsers = RegisteredUserService.listRegisteredUsers();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            if (contacts == null) {
                LoaderManager.getInstance(this).initLoader(0, null, this);
            }
        }

        initViews();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LoaderManager.getInstance(this).initLoader(0, null, this);
            } else {
                Log.e("ContactsFragment", "Permission not granted");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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
        restoreRecyclerViewState();
        reloadData();
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

    public void initViews() {

    }


    @Override
    public void clearData() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Override
    public void reloadData() {
        if (contacts != null) {
            loadAdapter();
            loadAddContactList();
        }
    }

    @Override
    public void filter(String text) {
        if (text == null) {
            text = "";
        }
        if (contacts == null) {
            String finalText = text;
            reloadData(() -> {
                applyFilter(finalText);
            });
            return;
        }
        applyFilter(text);
    }

    private void applyFilter(String text) {
        if (text.trim().isEmpty()) {
            if (contacts != null) {
                adapter1 = new ContactsAdapter(getActivity(), contacts, registeredUsers);
                if (recyclerView != null) {
                    recyclerView.setAdapter(adapter1);
                } else {
                    Log.e("ContactsFragment", "RecyclerView is null");
                }
            } else {
                Log.e("ContactsFragment", "Contacts list is null");
            }
            return;
        }

        List<Contact> filteredContacts = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getName() != null && contact.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredContacts.add(contact);
            }
        }

        adapter1 = new ContactsAdapter(getActivity(), filteredContacts, registeredUsers);
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter1);
        } else {
            Log.e("ContactsFragment", "RecyclerView is null");
        }
    }

    private void reloadData(Runnable onDataLoaded) {
        if (contacts == null) {
            LoaderManager.getInstance(this).restartLoader(0, null, this);
        }

        new Handler().postDelayed(onDataLoaded::run, 1000);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst() && contacts==null) {
            Set<String> addedContacts = new HashSet<>();
            contacts = new ArrayList<>();
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                String validPhoneNumber = validPhoneNumber(phoneNumber);

                if (!addedContacts.contains(validPhoneNumber)) {
                    Contact contact = new Contact(name, phoneNumber, photoUri);
                    contacts.add(contact);
                    addedContacts.add(validPhoneNumber);
                    if (registeredUsers.contains(validPhoneNumber)) {
                        registeredContacts.add(contact);
                    }
                }
            } while (cursor.moveToNext());

            if (!contacts.isEmpty()) {
                loadAdapter();
                loadAddContactList();
            } else {
                Log.e("ContactsFragment", "No contacts found.");
            }
        } else {
            Log.e("ContactsFragment", "Cursor is null or empty.");
        }
    }

    private void loadAddContactList() {
        for (Contact contact : registeredContacts) {
            dm.add(ChatSDK.core().getUserForEntityID(validPhoneNumber(contact.getNumber())).flatMapCompletable(user -> {
                user.setAvatarURL(contact.getPhoto());
                user.setName(contact.getName());
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
