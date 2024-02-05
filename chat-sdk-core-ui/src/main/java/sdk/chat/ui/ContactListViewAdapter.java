package sdk.chat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class ContactListViewAdapter extends ArrayAdapter<ContactList>  {

    Context context;
    ArrayList<ContactList> list;
    Set<String> registeredUsers;

    protected DisposableMap dm = new DisposableMap();
    public ContactListViewAdapter(Context context, ArrayList<ContactList> items, Set<String> registeredUsers){
        super(context, R.layout.user_row_test,items);
        this.context = context;
        list = items;
        this.registeredUsers = registeredUsers;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.e("GetView", "GetView Called");
        if(convertView == null) {


            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.user_row_test, null);
        }

        ImageView userImage = convertView.findViewById(R.id.userImage);
        TextView userContactName = convertView.findViewById(R.id.userContactName);
        TextView userContactNumber = convertView.findViewById(R.id.userContactNumber);
        TextView inviteText = convertView.findViewById(R.id.inviteText);
        LinearLayout linearLayout2 = convertView.findViewById(R.id.linearLayout2);


        //List<User> user = ChatSDK.currentUser().getContacts();


        System.out.println("valid phone numbers : " + validPhoneNumber(list.get(position).getContactNumber()) + " " + registeredUsers.contains(validPhoneNumber(list.get(position).getContactNumber())));

       if(registeredUsers.contains(validPhoneNumber(list.get(position).getContactNumber()))){

           inviteText.setText("");
       }
       else{
          inviteText.setText("Invite");
       }

       inviteText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               sendInvite(validPhoneNumber(list.get(position).getContactNumber()));
           }
       });

        userContactName.setText(list.get(position).getContactName());
        userContactNumber.setText(list.get(position).getContactNumber());

        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ContactProfile.class);
                intent.putExtra("contactName",list.get(position).getContactName());
                intent.putExtra("contactNumber",list.get(position).getContactNumber());
                if(registeredUsers.contains(validPhoneNumber(list.get(position).getContactNumber()))){
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
