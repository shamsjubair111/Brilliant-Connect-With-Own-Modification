package sdk.chat.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    Context context;
    List<Contact> list;
    Set<String> registeredUsers;

    public ContactRecyclerViewAdapter(Context context, List<Contact> items, Set<String> registeredUsers) {
        this.context = context;
        this.list = items;
        this.registeredUsers = registeredUsers;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_row_test, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact currentContact = list.get(position);

        Log.d("currentContact.getNumber()", currentContact.getNumber());

        if (registeredUsers.contains(currentContact.getNumber())) {
            holder.inviteText.setText("");
        } else {
            holder.inviteText.setText("Invite");
        }

        holder.inviteText.setOnClickListener(v -> {
            try {
                sendInvite(validPhoneNumber(currentContact.getNumber()));
            } catch (NumberParseException e) {
                throw new RuntimeException(e);
            }
        });

        if (currentContact.getPhoto() != null) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, currentContact.getId());
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                byte[] photoData = cursor.getBlob(0);
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                holder.userImage.setImageBitmap(photoBitmap);
                cursor.close();
                holder.letterImage.setVisibility(View.GONE);
            }
        } else {
            holder.letterImage.setVisibility(View.VISIBLE);
            String[] splittedArray = currentContact.getName().trim().split("[\\s]+");
            String st = (splittedArray.length < 2) ? String.valueOf(splittedArray[0].charAt(0)) : splittedArray[0].charAt(0) + "" + splittedArray[1].charAt(0);
            holder.letterImage.setText(st.toUpperCase());
            holder.userImage.setImageResource(R.drawable.profile_circle);
        }

        holder.userContactName.setText(currentContact.getName());
        holder.userContactNumber.setText(currentContact.getNumber());

        holder.constraintLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactProfile.class);
            intent.putExtra("contactName", currentContact.getName());
            intent.putExtra("contactNumber", currentContact.getNumber());
            try {
                if (registeredUsers.contains(validPhoneNumber(currentContact.getNumber()))) {
                    intent.putExtra("registered", "yes");
                } else {
                    intent.putExtra("registered", "no");
                }
            } catch (NumberParseException e) {
                throw new RuntimeException(e);
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static String validPhoneNumber(String mobileNumber) throws NumberParseException {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(mobileNumber, "BD");
        mobileNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        Log.d("mobileNumber", mobileNumber);
        return mobileNumber;
    }

    public void sendInvite(String to) {
        OkHttpClient client = new OkHttpClient();
        String invitationMsg = "Install Brilliant Connect from Play Store: \n https://play.google.com/store/apps/details?id=com.brilliant.connect.com.bd&hl=en&gl=US&pli=1";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String json = "{\n" +
                "    \"from\": \"TelcoBright\",\n" +
                "    \"to\": \"" + to + "\",\n" +
                "    \"Content\": \"" + invitationMsg + "\",\n" +
                "}";

        RequestBody body = RequestBody.create(mediaType, json);

        String url = "https://appsrv.intercloud.com.bd/test/api/VendorOTP/SendOTP"; // Replace with your actual API URL
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("login", "TelcoBright")
                .addHeader("password", "IO&3(DF&")
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            } else {
                // Successful response, handle accordingly
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception
        }
    }
}