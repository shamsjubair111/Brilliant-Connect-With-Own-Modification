package sdk.chat.firebase.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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


        Intent remoteIntent = remoteMessage.toIntent();
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
                else if (messageType.equals("-1"))
                {
                    messageType = "cancel";
                }
            }
        }
        catch (Exception e)
        {
            Log.d("Default Message", "onMessageReceived() called with: remoteMessage = [" + remoteMessage + "]");
        }



        String senderNumber = remoteMessage.getData().get("chat_sdk_push_title");

        if(messageType.equals("audio")||messageType.equals("video")) {
            if (!ChatSDK.db().isDatabaseOpen()) {
                String currentUserId = ChatSDK.auth().getCurrentUserEntityID();
                if (currentUserId != null) {
                    try {
                        ChatSDK.db().openDatabase(currentUserId);
                    } catch (Exception e) {

                    }
                }
            }
            ChatSDK.mediaStart(true,getApplicationContext());



            Intent fullScreenIntent = new Intent(getApplicationContext(), IncomingCallActivity.class);
            fullScreenIntent.putExtra("senderNumber",senderNumber);
            fullScreenIntent.putExtra("type",messageType);
            PendingIntent fullScreenPendingIntent ;


            final Context context = ChatSDK.ctx();


            fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            Intent answerIntent = new Intent(this, ReceiverActivity.class);
            answerIntent.putExtra("senderNumber",senderNumber);
            answerIntent.putExtra("type",messageType);



            Intent appIntent = new Intent(getApplicationContext(), IncomingCallActivity.class);
            appIntent.putExtra("senderNumber", senderNumber);
            appIntent.putExtra("type",messageType);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);



            Intent deleteIntent = new Intent(getApplicationContext(), NotificationCancelActivity.class);
            deleteIntent.putExtra("senderNumber", senderNumber);
            deleteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);


            NotificationDisplayHandler notification = new NotificationDisplayHandler();
            notification.createCallNotification(getApplicationContext(), appIntent, senderNumber, senderNumber,fullScreenPendingIntent,messageType,answerIntent,deleteIntent);
        }
        else if(messageType.equals("cancel"))
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                notificationManager.cancel(100001);
                ChatSDK.mediaStop();
                IncomingCallActivity incomingCallActivity = (IncomingCallActivity) ChatSDK.callActivities.get("incomingCallActivity");
                if(incomingCallActivity!=null)
                {
                    incomingCallActivity. finishAndRemoveTask();
                }
//            }

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
