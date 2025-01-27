package sdk.chat.ui;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContactUtils {


    public static ArrayList<ContactList> contactArrayList = null;

    public static void getContacts(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            contactArrayList = new ArrayList<>();

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

//                Log.d("ContactUtils", "Contact Name: " + contactName);

                //Get Image
                Bitmap getContactPhoto = getContactPhoto(contentResolver, contactId);
                // Get phone numbers for the contact
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToNext()) {
                    @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    Log.d("ContactUtils", "Contact Number: " + phoneNumber);


                    ContactList contactList = new ContactList(getContactPhoto,contactName,phoneNumber);
                    contactArrayList.add(contactList);
//
                    Collections.sort(contactArrayList, new Comparator<ContactList>() {
                        @Override
                        public int compare(ContactList contact1, ContactList contact2) {
                            return contact1.getContactName().compareToIgnoreCase(contact2.getContactName());
                        }
                    });

                    phoneCursor.close();
                }
            }
            cursor.close();
        }
    }
    private static Bitmap getContactPhoto(ContentResolver contentResolver, String contactId) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactUri);

        if (inputStream != null) {
            return BitmapFactory.decodeStream(inputStream);
        }
        return null;
    }
}
