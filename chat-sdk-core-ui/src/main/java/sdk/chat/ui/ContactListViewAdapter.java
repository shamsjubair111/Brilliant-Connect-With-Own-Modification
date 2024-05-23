package sdk.chat.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.ui.api.RegisteredUserService;
import sdk.chat.ui.fragments.ChatFragment;
import sdk.guru.common.DisposableMap;
import android.database.Cursor;

public class ContactListViewAdapter extends ArrayAdapter<Contact>  {

    Context context;
    List<Contact> list;
    Set<String> registeredUsers;

    protected DisposableMap dm = new DisposableMap();
    public ContactListViewAdapter(Context context, List<Contact> items, Set<String> registeredUsers){
        super(context, R.layout.user_row_test,items);
        this.context = context;
        list = items;
        this.registeredUsers = registeredUsers;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        if(convertView == null) {


            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.user_row_test, null);
        }

        ImageView userImage = convertView.findViewById(R.id.userImage);
        TextView letterImage = convertView.findViewById(R.id.letterImage);
        TextView userContactName = convertView.findViewById(R.id.userContactName);
        TextView userContactNumber = convertView.findViewById(R.id.userContactNumber);
        TextView inviteText = convertView.findViewById(R.id.inviteText);
        LinearLayout linearLayout2 = convertView.findViewById(R.id.linearLayout2);
        ConstraintLayout constraintLayout = convertView.findViewById(R.id.constraintLayout2);


        //List<User> user = ChatSDK.currentUser().getContacts();


//        System.out.println("valid phone numbers : " + list.get(position).getNumber() + " " + registeredUsers.contains(list.get(position).getNumber()));

       if(registeredUsers.contains(list.get(position).getNumber())){

           inviteText.setText("");
       }
       else{
          inviteText.setText("Invite");
       }

       inviteText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               sendInvite(validPhoneNumber(list.get(position).getNumber()));
           }
       });

        Contact currentContact = list.get(position);
        if (currentContact.getPhoto() != null) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, currentContact.getId());
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                byte[] photoData = cursor.getBlob(0);
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                userImage.setImageBitmap(photoBitmap);
                cursor.close();
            }
            letterImage.setVisibility(View.GONE);
        } else {
            letterImage.setVisibility(View.VISIBLE);
            String[] splittedArray = currentContact.getName().trim().split("[\\s]+");
            String st = (splittedArray.length<2) ? String.valueOf(splittedArray[0].charAt(0)) : splittedArray[0].charAt(0) + "" + splittedArray[1].charAt(0);
            letterImage.setText(st.toUpperCase());
            userImage.setImageResource(R.drawable.profile_circle);
//            userImage.setColorFilter(android.R.color.darker_gray);
        }
        userContactName.setText(list.get(position).getName());
        userContactNumber.setText(list.get(position).getNumber());

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ContactProfile.class);
                intent.putExtra("contactName",list.get(position).getName());
                intent.putExtra("contactNumber",list.get(position).getNumber());
                if(registeredUsers.contains(validPhoneNumber(list.get(position).getNumber()))){
                    intent.putExtra("registered","yes");
                }
                else{
                    intent.putExtra("registered","no");
                }

                getContext().startActivity(intent);
            }
        });


//        ImageButton videoCall = convertView.findViewById(R.id.videoCall);
//
//        ImageButton audioCall = convertView.findViewById(R.id.audioCall);
//
//        ImageButton sipCallButton = convertView.findViewById(R.id.sipCallButton);

//        videoCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(),VideoActivity.class);
//                intent.putExtra("type","video");
//                getContext().startActivity(intent);
//
//            }
//        });


//        audioCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(),VideoActivity.class);
//                intent.putExtra("type","audio");
//                getContext().startActivity(intent);
//
//            }
//        });


//        sipCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(),AudioActivity.class);
//                intent.putExtra("callee", validPhoneNumber(list.get(position).getContactNumber()));
//
//                getContext().startActivity(intent);
//            }
//        });



        return convertView;

    }

    public static String  validPhoneNumber(String mobileNumber) {

        mobileNumber = mobileNumber.replaceAll("[\\s-]+", "");
        if(mobileNumber.length()<11)
            return mobileNumber;
        mobileNumber = mobileNumber.substring(mobileNumber.length() - 11);
        mobileNumber = "88" + mobileNumber;

        return mobileNumber;
    }

    public void sendInvite(String to) {
        OkHttpClient client = new OkHttpClient();
        String invitationMsg = "Install Brilliant Connect from Play Store: \\n https://play.google.com/store/apps/details?id=com.brilliant.connect.com.bd&hl=en&gl=US&pli=1";
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
