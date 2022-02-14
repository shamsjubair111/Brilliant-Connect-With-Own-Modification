package sdk.chat.core.rigs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.UploadStatus;
import sdk.chat.core.types.FileUploadResult;
import sdk.chat.core.types.MessageSendProgress;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.guru.common.RX;

public class MessageSendRig {

    public static String linkedFileKey = "linked-file";

    public interface MessageDidUploadUpdateAction {
        void update (Message message, FileUploadResult result);
    }

    public interface MessageDidCreateUpdateAction {
        void update (Message message);
    }

    protected MessageType messageType;
    protected Thread thread;
    protected Message message;
    protected ArrayList<Uploadable> uploadables = new ArrayList<>();
    protected boolean local = false;

    // This is called after the text has been created. Use it to set the text's payload
    protected MessageDidCreateUpdateAction messageDidCreateUpdateAction;

    // This is called after the text had been uploaded. Use it to update the text payload's url data
    protected MessageDidUploadUpdateAction messageDidUploadUpdateAction;

    public MessageSendRig(MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
        this.messageType = type;
        this.thread = thread;
        this.messageDidCreateUpdateAction = action;
    }

    public MessageSendRig(Message message, Thread thread) {
        this.thread = thread;
        this.message = message;
    }

    public MessageSendRig(Message message) {
        this.thread = message.getThread();
        this.message = message;

        // If upload failed
        if (message.uploadFailed()) {

            // Re-upload any files that failed before
            List<CachedFile> files = ChatSDK.uploadManager().getFiles(message.getEntityID());
            for (CachedFile file: files) {
                if (!file.completeAndValid()) {
                    Uploadable uploadable = file.getUploadable();
                    if (uploadable != null) {
                        uploadables.add(uploadable);
                        file.setUploadStatus(UploadStatus.WillStart);
                        file.update();
                    }
                }
            }
            message.setMessageStatus(MessageSendStatus.Created, true);
        }
    }

    public static MessageSendRig create(MessageType type, Thread thread, MessageDidCreateUpdateAction action) {
        return new MessageSendRig(type, thread, action);
    }

    public static MessageSendRig create(Message message, Thread thread) {
        return new MessageSendRig(message, thread);
    }

    public static MessageSendRig create(Message message) {
        return new MessageSendRig(message);
    }

    public MessageSendRig setUploadables (MessageDidUploadUpdateAction messageDidUploadUpdateAction, Uploadable... uploadables) {
        return this.setUploadables(Arrays.asList(uploadables), messageDidUploadUpdateAction);
    }

    public MessageSendRig setUploadables (List<Uploadable> uploadables, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        this.uploadables.addAll(uploadables);
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig setUploadable(Uploadable uploadable, MessageDidUploadUpdateAction messageDidUploadUpdateAction) {
        uploadables.add(uploadable);
        this.messageDidUploadUpdateAction = messageDidUploadUpdateAction;
        return this;
    }

    public MessageSendRig localOnly() {
        local = true;
        return this;
    }

    public Completable run() {
        return ChatSDK.messageSender().run(this);
    }

    public Completable doRun() {
        return Completable.defer(() -> {
            if (message == null) {
                createMessage();
            }
            message.setMessageStatus(MessageSendStatus.Created, true);
            if (uploadables.isEmpty()) {
                return send();
            } else {
                // First pass back an empty result so that we add the cell to the table view
//                message.setMessageStatus(MessageSendStatus.WillUpload);

                ArrayList<Uploadable> compressedUploadables = new ArrayList<>();

                for (Uploadable item : uploadables) {
                    compressedUploadables.add(item.compress());
                }

                uploadables.clear();
                uploadables.addAll(compressedUploadables);

                return uploadFiles().andThen(send());
            }
        }).subscribeOn(RX.quick());
    }

    protected Message createMessage() {
        message = ChatSDK.thread().newMessage(messageType.value(), thread);
        if (messageDidCreateUpdateAction != null) {
            messageDidCreateUpdateAction.update(message);
        }
        return message;
    }

    protected Completable send() {
        return Completable.defer(() -> {
            message.setMessageStatus(MessageSendStatus.WillSend);
            return ChatSDK.hook().executeHook(HookEvent.MessageWillSend, new HashMap<String, Object>() {{
                put(HookEvent.Message, message);
            }}).andThen(Completable.defer(() -> {
                if (local) {
                    return Completable.complete();
                } else {
                    return ChatSDK.thread().sendMessage(message);
                }
            }));
        }).subscribeOn(RX.quick()).andThen(ChatSDK.hook().executeHook(HookEvent.MessageSent, new HashMap<String, Object>() {{
            put(HookEvent.Message, message);
        }})).doOnComplete(() -> {
            message.setMessageStatus(MessageSendStatus.Sent);
        }).doOnError(throwable -> {
            message.setMessageStatus(MessageSendStatus.Failed);
        });
    }

    protected Completable uploadFiles() {
        return Single.create((SingleOnSubscribe<List<Completable>>) emitter -> {
            ArrayList<Completable> completables = new ArrayList<>();

            message.setMessageStatus(MessageSendStatus.WillUpload);

            List<Uploadable> toUpload = new ArrayList<>();

            // Add the files to the upload manager
            for (Uploadable item : uploadables) {
                CachedFile file = ChatSDK.uploadManager().add(item, message);

//                if (file.completeAndValid()) {
//                    message.setValueForKey(file.getRemotePath(), item.messageKey);
//                } else {
//                    file.setUploadStatus(UploadStatus.WillStart);
//                    toUpload.add(item);
//                }
                if (!file.completeAndValid()) {
                    file.setUploadStatus(UploadStatus.WillStart);
                    toUpload.add(item);
                }
            }

            message.setMessageStatus(MessageSendStatus.Uploading);

            for (Uploadable item : toUpload) {
                completables.add(ChatSDK.upload().uploadFile(item.getBytes(), item.name, item.mimeType, item.hash()).flatMapMaybe(result -> {

                    ChatSDK.uploadManager().setStatus(item.hash(), result.status);

                    if (item.reportProgress) {
                        ChatSDK.events().source().accept(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message, MessageSendStatus.Uploading, result.progress)));
                    }

                    // Success
                    if (result.urlValid()) {
                        if (messageDidUploadUpdateAction != null) {
                            messageDidUploadUpdateAction.update(message, result);
                        }

                        message.setValueForKey(result.url, item.messageKey);

                        for (String key : result.meta.keySet()) {
                            message.setValueForKey(result.meta.get(key), key);
                        }

                        CachedFile file = ChatSDK.uploadManager().getFile(item.hash());
                        if (file != null) {
                            file.setRemotePath(result.url);
                            file.update();
                        }

                        return Maybe.just(message);
                    } else {
                        return Maybe.empty();
                    }
                }).doOnError(throwable -> {
                    message.setMessageStatus(MessageSendStatus.UploadFailed);
                }).firstElement().ignoreElement());
            }
            emitter.onSuccess(completables);
        }).subscribeOn(RX.quick()).flatMapCompletable(Completable::merge).doOnComplete(() -> {
            message.setMessageStatus(MessageSendStatus.DidUpload);
        });
    }

    public void finalize() {
        System.out.println("Finalize");
    }

}
