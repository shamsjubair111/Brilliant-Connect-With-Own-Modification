package sdk.chat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactProfile extends AppCompatActivity {

    private  TextView userNameTextView;
    private TextView textView9;

    private ImageView backImage;
    private ImageView videoCall;

    private  ImageView imageView3;

    private ImageView appAudioCall;

    public static String validPhoneNumber(String mobileNumber) {
        mobileNumber = mobileNumber.replaceAll("[\\s-]+", "");
        if(mobileNumber.length()<11)
            return mobileNumber;
        mobileNumber = mobileNumber.substring(mobileNumber.length() - 11);
        mobileNumber = "88" + mobileNumber;

        return mobileNumber;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        userNameTextView = findViewById(R.id.userNameTextView);
        textView9 = findViewById(R.id.textView9);
        backImage = findViewById(R.id.backImage);
        videoCall = findViewById(R.id.videoCall);
        imageView3 = findViewById(R.id.imageView3);
        appAudioCall = findViewById(R.id.imageView7);

        userNameTextView.setText(getIntent().getStringExtra("contactName"));

        textView9.setText(getIntent().getStringExtra("contactNumber"));
        String receiverNumber = validPhoneNumber(getIntent().getStringExtra("contactNumber"));

        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getIntent().getStringExtra("registered").equals("yes")){
                    Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                    intent.putExtra("receiverNumber",receiverNumber);
                    intent.putExtra("type","video");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });


        appAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getStringExtra("registered").equals("yes")){
                    Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
                    intent.putExtra("receiverNumber",receiverNumber);
                    intent.putExtra("type","audio");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });




        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(ContactProfile.this, AudioActivity.class);
                intent.putExtra("callee",validPhoneNumber(getIntent().getStringExtra("contactNumber")));
                startActivity(intent);
            }
        });


        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}