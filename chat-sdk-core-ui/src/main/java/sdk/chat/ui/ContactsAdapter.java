package sdk.chat.ui;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final Context context;
    private final Set<String> registeredUsers;
    private final List<String> colors = Arrays.asList("#A1DD70", "#EE4E4E", "#E49BFF", "#3ABEF9", "#ffffff", "#FF7F3E");
    private List<Contact> contacts;

    public ContactsAdapter(Context context, List<Contact> contacts, Set<String> registeredUsers) {
        this.context = context;
        this.contacts = contacts;
        this.registeredUsers = registeredUsers;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);

        String contactName = contact.getName();
        String contactNumber = contact.getNumber();
        String contactPhoto = contact.getPhoto();

        String validContactNumber = validPhoneNumber(contactNumber);


        if (registeredUsers.contains(validContactNumber)) {
            holder.inviteText.setText("");
        } else {
            holder.inviteText.setText("Invite");
        }

        holder.inviteText.setOnClickListener(v -> sendInvite(validContactNumber));

        holder.contactName.setText(contactName);
        holder.contactNumber.setText(contactNumber);

        if (contactPhoto != null) {
            holder.contactPhoto.setImageURI(Uri.parse(contactPhoto));
            holder.contactPhoto.setVisibility(View.VISIBLE);
            holder.letterImage.setVisibility(View.GONE);
        } else {
            String[] splitArray = contactName.trim().split("\\s+");
            String st = (splitArray.length < 2) ? String.valueOf(splitArray[0].charAt(0)) : String.valueOf(splitArray[0].charAt(0)) + splitArray[1].charAt(0);

            holder.letterImage.setText(st.toUpperCase(Locale.getDefault()));
            holder.letterImage.setTextColor(Color.parseColor(colors.get(position % colors.size())));
            holder.letterImage.setTypeface(null, Typeface.BOLD);
            holder.contactPhoto.setVisibility(View.GONE);
            holder.letterImage.setVisibility(View.VISIBLE);
        }

        holder.rootLinearLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactProfile.class);
            intent.putExtra("contactName", contactName);
            intent.putExtra("contactNumber", contactNumber);

            if (contactPhoto != null) {
                intent.putExtra("contactImage", contactPhoto);
            } else{
                intent.putExtra("contactImage", "");
            }

            if (registeredUsers.contains(validContactNumber)) {
                intent.putExtra("registered", "yes");
            } else {
                intent.putExtra("registered", "no");
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void sendInvite(String to) {
        OkHttpClient client = new OkHttpClient();
        String invitationMsg = "Install Brilliant Connect from Play Store: \n https://play.google.com/store/apps/details?id=com.brilliant.connect.com.bd&hl=en&gl=US&pli=1";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from", "TelcoBright");
            jsonObject.put("to", to);
            jsonObject.put("Content", invitationMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String json = jsonObject.toString();

        RequestBody body = RequestBody.create(mediaType, json);
        String url = "https://appsrv.intercloud.com.bd/test/api/VendorOTP/SendOTP";

        Request request = new Request.Builder().url(url).post(body).addHeader("login", "TelcoBright").addHeader("password", "IO&3(DF&").build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;
        public ImageView contactPhoto;
        public TextView letterImage;
        public TextView inviteText;
        public LinearLayout rootLinearLayout;

        public ViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.userContactName);
            contactNumber = view.findViewById(R.id.userContactNumber);
            contactPhoto = view.findViewById(R.id.userImage);
            letterImage = view.findViewById(R.id.letterImage);
            inviteText = view.findViewById(R.id.inviteText);
            rootLinearLayout = view.findViewById(R.id.rootLinearLayout);
        }
    }
}
