package sdk.chat.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ContactViewHolder extends RecyclerView.ViewHolder {
    ImageView userImage;
    TextView letterImage;
    TextView userContactName;
    TextView userContactNumber;
    TextView inviteText;
    ConstraintLayout constraintLayout;

    public ContactViewHolder(@NonNull View itemView) {
        super(itemView);
        userImage = itemView.findViewById(R.id.userImage);
        letterImage = itemView.findViewById(R.id.letterImage);
        userContactName = itemView.findViewById(R.id.userContactName);
        userContactNumber = itemView.findViewById(R.id.userContactNumber);
        inviteText = itemView.findViewById(R.id.inviteText);
        constraintLayout = itemView.findViewById(R.id.constraintLayout2);
    }
}