package com.codewithkael.webrtcprojectforrecord.models;

import com.codewithkael.webrtcprojectforrecord.utils.NewJanusMessageInterface;

public interface JanusCallHandlerInterface extends NewJanusMessageInterface {
    void handleSentMessage(String message);
    void handleReceivedMessage(String message);
    void createSession();
    void hangup();

//    void sendMessage(String message);
}

