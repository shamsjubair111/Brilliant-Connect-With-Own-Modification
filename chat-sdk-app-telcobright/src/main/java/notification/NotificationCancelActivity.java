package notification;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.example.chat_sdk_app_telcobright.R;

import java.util.HashMap;

import callHandler.TelcobrightCallMessage;
import sdk.chat.core.session.ChatSDK;

public class NotificationCancelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(100001);
        ChatSDK.mediaStop();
        this.finish();
    }
}