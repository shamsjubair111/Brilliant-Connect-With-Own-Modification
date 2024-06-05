package callHandler;

import androidx.annotation.Nullable;

import org.pmw.tinylog.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.ThreadHandler;
import sdk.chat.core.interfaces.SystemMessageType;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.handlers.ThreadHandler;

public  class TelcobrightCallMessage {
    protected Message message;
    protected MessageType messageType;
    Thread thread;
    public static String UserIds = "userIds";

    public static String Type = "type";
    public static String Body = "body";
    public static String SenderId = "senderId";
    public static String SenderName = "senderName";
    public static String ThreadId = "threadId";
    public static String Action = "action";
    public static String EncryptedMessage = "encrypted-message";

    protected MessageSendRig.MessageDidCreateUpdateAction messageDidCreateUpdateAction;

    // This is called after the text had been uploaded. Use it to update the text payload's url data
    protected MessageSendRig.MessageDidUploadUpdateAction messageDidUploadUpdateAction;

    public HashMap<String, Object> createMessage(String threadId, String senderName, String senderId, HashMap userIds,String action, String body,int type) {

        HashMap<String,String> users = new HashMap<String,String>();

        users.put(threadId,senderId);

        HashMap<String, Object> data = new HashMap<>();
        data.put(UserIds, userIds);
        data.put(Body, body);
        data.put(SenderName, ChatSDK.auth().getCurrentUserEntityID().split("@")[0]);
        data.put(Type, type);
        data.put(SenderId, senderId);
        data.put(ThreadId, senderId);
        data.put(Action,action);
        data.put(EncryptedMessage,"");
        return data;
    }

    public static Completable videoCall(String sender, String receiver, Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.VideoCall), thread, message -> {
            message.setText("Video called From :" + sender);
        }).run();
    }


}
