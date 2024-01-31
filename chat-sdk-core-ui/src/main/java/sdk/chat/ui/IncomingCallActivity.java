package sdk.chat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import sdk.chat.core.session.ChatSDK;

public class IncomingCallActivity extends AppCompatActivity {

    private ImageButton activity_hang_up_button;
    private ImageButton activity_answer_call_button;

    private TextView caller_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        activity_hang_up_button = findViewById(R.id.activity_hang_up_button);
        activity_answer_call_button = findViewById(R.id.activity_answer_call_button);
        caller_name = findViewById(R.id.caller_name);

        caller_name.setText(getIntent().getStringExtra("senderNumber"));

        activity_hang_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(100001);
                finish();
                ChatSDK.mediaStop();
            }
        });


        activity_answer_call_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ChatSDK.mediaStop();
                String senderNumber = getIntent().getStringExtra("senderNumber");
                Intent intent = new Intent(getApplicationContext(), ReceiverActivity.class);
                intent.putExtra("senderNumber", senderNumber);
                intent.putExtra("type", "video");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}