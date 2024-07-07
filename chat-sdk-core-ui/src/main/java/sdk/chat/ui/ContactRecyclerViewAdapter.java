package sdk.chat.ui;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private final Context context;
    private List<Contact> list;
    private final Set<String> registeredUsers;
    private final LayoutInflater inflater;

    private final List<String> colors = Arrays.asList(
            "#A1DD70", "#EE4E4E", "#E49BFF", "#3ABEF9", "#ffffff", "#FF7F3E"
    );

    private final List<Integer> imageList = Arrays.asList(
            R.drawable.ragnar, R.drawable.sazid_vai, R.drawable.suchi_apu,
            R.drawable.maruf_vai, R.drawable.angela_merkel, R.drawable.joe_biden,
            R.drawable.donald_trump, R.drawable.messi, R.drawable.ronaldo
    );

    public ContactRecyclerViewAdapter(Context context, List<Contact> items, Set<String> registeredUsers) {
        this.context = context;
        this.list = items;
        this.registeredUsers = registeredUsers;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.user_row_test, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        try {
            Contact contact = list.get(position);

            String contactNumber = contact.getNumber();
            String validContactNumber = validPhoneNumber(contactNumber);

            if (registeredUsers.contains(validContactNumber)) {
                holder.inviteText.setText("");
            } else {
                holder.inviteText.setText("Invite");
            }

            holder.inviteText.setOnClickListener(v -> sendInvite(validContactNumber));

            if (contact.getPhoto() != null) {
                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
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
                String[] splitArray = contact.getName().trim().split("\\s+");
                String st = (splitArray.length < 2)
                        ? String.valueOf(splitArray[0].charAt(0))
                        : String.valueOf(splitArray[0].charAt(0)) + splitArray[1].charAt(0);

                if (position % 2 == 0) {
                    holder.userImage.setImageResource(imageList.get(position % imageList.size()));
                    holder.letterImage.setVisibility(View.GONE);
                } else {
                    holder.letterImage.setText(st.toUpperCase(Locale.getDefault()));
                    holder.letterImage.setTextColor(Color.parseColor(colors.get(position % colors.size())));
                    holder.letterImage.setTypeface(null, Typeface.BOLD);
                    holder.letterImage.setVisibility(View.VISIBLE);
                }
            }

            holder.userContactName.setText(contact.getName());
            holder.userContactNumber.setText(contactNumber);

            holder.constraintLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, ContactProfile.class);
                intent.putExtra("contactName", contact.getName());
                intent.putExtra("contactNumber", contactNumber);

                if (contact.getPhoto() != null) {
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
                    Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                    Cursor cursor = context.getContentResolver().query(photoUri,
                            new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        byte[] photoData = cursor.getBlob(0);
                        intent.putExtra("contactImage", photoData);
                        cursor.close();
                    }
                } else {
                    intent.putExtra("imageResId", position);
                }

                if (registeredUsers.contains(validContactNumber)) {
                    intent.putExtra("registered", "yes");
                } else {
                    intent.putExtra("registered", "no");
                }

                context.startActivity(intent);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void sendInvite(String to) {
        OkHttpClient client = new OkHttpClient();
        String invitationMsg = "Install Brilliant Connect from Play Store: \n https://play.google.com/store/apps/details?id=com.brilliant.connect.com.bd&hl=en&gl=US&pli=1";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String json = "{\n" +
                "    \"from\": \"TelcoBright\",\n" +
                "    \"to\": \"" + to + "\",\n" +
                "    \"Content\": \"" + invitationMsg + "\",\n" +
                "}";

        RequestBody body = RequestBody.create(mediaType, json);
        String url = "https://appsrv.intercloud.com.bd/test/api/VendorOTP/SendOTP";

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}