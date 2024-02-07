package sdk.chat.firebase.push;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import notification.NotificationCancelActivity;
import notification.NotificationDisplayHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.push.BroadcastHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.IncomingCallActivity;
import sdk.chat.ui.ReceiverActivity;

public class DefaultMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


//        MediaPlayer player = PhoneControl.getMediaPlayer(getApplicationContext());
//        player.start();
        Intent remoteIntent= remoteMessage.toIntent();
        Bundle extras = remoteIntent.getExtras();

        final String threadEntityID = extras.getString(Keys.PushKeyThreadEntityID);
        final String userEntityID = extras.getString(Keys.PushKeyUserEntityID);
        String messageType = "default";
        try {
            if(remoteMessage.getData().get("chat_sdk_push_type").toString()!=null)
            {
                messageType =  remoteMessage.getData().get("chat_sdk_push_type").toString();
                if(messageType.equals("100"))
                {
                    messageType = "audio";
                }
                else if (messageType.equals("101"))
                {
                    messageType = "video";
                }
            }
        }
        catch (Exception e)
        {
            Log.d("Default Message", "onMessageReceived() called with: remoteMessage = [" + remoteMessage + "]");
        }



        String senderNumber = remoteMessage.getData().get("chat_sdk_push_title");

        if(messageType.equals("audio")||messageType.equals("video")) {
// If the database is not open...
            if (!ChatSDK.db().isDatabaseOpen()) {
                String currentUserId = ChatSDK.auth().getCurrentUserEntityID();
                if (currentUserId != null) {
                    try {
                        ChatSDK.db().openDatabase(currentUserId);
                    } catch (Exception e) {

                    }
                }
            }
            ChatSDK.mediaStart(true);

            Intent fullScreenIntent = new Intent(getApplicationContext(), IncomingCallActivity.class);
            fullScreenIntent.putExtra("senderNumber",senderNumber);
            fullScreenIntent.putExtra("type",messageType);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                fullScreenIntent, PendingIntent.FLAG_IMMUTABLE);


            Intent answerIntent = new Intent(this, ReceiverActivity.class);
            answerIntent.putExtra("senderNumber",senderNumber);
            answerIntent.putExtra("type",messageType);



            Intent appIntent = new Intent(getApplicationContext(), IncomingCallActivity.class);
            appIntent.putExtra("senderNumber", senderNumber);
            appIntent.putExtra("type",messageType);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);



            Intent deleteIntent = new Intent(getApplicationContext(), NotificationCancelActivity.class);
            deleteIntent.putExtra("senderNumber", senderNumber);
            deleteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


            NotificationDisplayHandler notification = new NotificationDisplayHandler();
            notification.createCallNotification(getApplicationContext(), appIntent, senderNumber, senderNumber,fullScreenPendingIntent,messageType,answerIntent,deleteIntent);

        }
        else {
            if (ChatSDK.shared().isValid() && !ChatSDK.config().manualPushHandlingEnabled) {
                for (BroadcastHandler handler : ChatSDK.shared().broadcastHandlers()) {
                    if (handler.onReceive(getApplicationContext(), remoteMessage.toIntent())) {
                        break;
                    }
                }
            }
        }
        }

}
