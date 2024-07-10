package sdk.chat.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<Contact> contacts;

    public ContactsAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactNumber.setText(contact.getNumber());
        if (contact.getPhoto() != null) {
            holder.contactPhoto.setImageURI(Uri.parse(contact.getPhoto()));
        } else {
            //holder.contactPhoto.setImageResource(R.drawable.default_contact_photo); // Default photo if no photo URI
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;
        public ImageView contactPhoto;

        public ViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.contact_name);
            contactNumber = view.findViewById(R.id.contact_number);
            contactPhoto = view.findViewById(R.id.contact_photo);
        }
    }
}
