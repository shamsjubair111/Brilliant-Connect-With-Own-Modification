package sdk.chat.firebase.push;

import android.app.IntentService;
import android.content.Intent;

public class CallReceiverService extends IntentService {

    public static final String ACTION_ACCEPT = "accept_call";
    public static final String ACTION_REJECT = "reject_call";

    public CallReceiverService() {
        super("CallReceiverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                String callId = intent.getStringExtra("callId");
                if (callId != null) {
                    if (action.equals(ACTION_ACCEPT)) {
                        // Handle accepting the call
                        acceptCall(callId);
                    } else if (action.equals(ACTION_REJECT)) {
                        // Handle rejecting the call
                        rejectCall(callId);
                    }
                }
            }
        }
    }

    private void acceptCall(String callId) {
        // Implement logic to accept the call using the callId
    }

    private void rejectCall(String callId) {
        // Implement logic to reject the call using the callId
    }
}