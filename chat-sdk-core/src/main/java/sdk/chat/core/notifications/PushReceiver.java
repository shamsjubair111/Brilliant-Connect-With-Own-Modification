package sdk.chat.core.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;

public class PushReceiver extends BroadcastReceiver {
    public static String EXTRA_CONVERSATION_ENTITY_ID_KEY = "conversation_id";
    public static String REMOTE_INPUT_RESULT_KEY = "reply_input";
    public static String MESSAGING_SERVICE_NAME = "sdk.chat.MessagingService";

    @Override
    public void onReceive(Context context, Intent intent) {

        String threadEntityID = intent.getStringExtra(EXTRA_CONVERSATION_ENTITY_ID_KEY);

        final Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);

        String action = intent.getAction();

        if (action != null && action.equals(ActionKeys.REPLY)) {

            Bundle results = RemoteInput.getResultsFromIntent(intent);

            if (results != null) {
                String message = results.getString(REMOTE_INPUT_RESULT_KEY);
                ChatSDK.thread().sendMessageWithText(message, thread).subscribe(ChatSDK.events());
            }
        }
        if (action != null && action.equals(ActionKeys.MARK_AS_READ)) {
            thread.markRead();
        }
    }
}
