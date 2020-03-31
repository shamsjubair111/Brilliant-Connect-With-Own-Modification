package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.EntityBareJid;
import org.pmw.tinylog.Logger;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.XMPPMessageParser;
import co.chatsdk.xmpp.utils.XMPPMessageWrapper;
import io.reactivex.disposables.Disposable;


public class XMPPMessageListener implements IncomingChatMessageListener, OutgoingChatMessageListener {

    public boolean isOneToOneMessage (Message xmppMessage) {
        return (xmppMessage.getType() == Message.Type.chat || xmppMessage.getType() == Message.Type.normal) && xmppMessage.getBody() != null;
    }

    @Override
    public void newIncomingMessage(EntityBareJid fromJID, Message message, Chat chat) {
        // Get the thread here before we parse the message. If the thread is null,
        // it will be created when we parse the message

        if(message.getLanguage() == null){
            message.setLanguage("en");
        }

        String from = fromJID.asBareJid().toString();

        final Thread thread = ChatSDK.db().fetchThreadWithEntityID(from);

        if(isOneToOneMessage(message)) {
            XMPPMessageParser.addMessageToThread(thread, XMPPMessageWrapper.with(message), from);
//            Disposable d = XMPPMessageParser.parse(message).subscribe((message1, throwable) -> {
//            });
        }

        ChatStateExtension chatState = (ChatStateExtension) message.getExtension(ChatStateExtension.NAMESPACE);
        if(chatState != null) {
            User user = ChatSDK.db().fetchUserWithEntityID(from);
            XMPPManager.shared().typingIndicatorManager.handleMessage(message, user);
            Logger.debug("Chat State: " + chatState.getChatState());
        }
    }

    @Override
    public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
        Logger.debug("Outgoing Message");
    }
}