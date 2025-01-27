package com.codewithkael.webrtcprojectforrecord.utils;

import com.codewithkael.webrtcprojectforrecord.models.JanusResponse;
import com.codewithkael.webrtcprojectforrecord.models.MessageModel;

import org.json.JSONException;

import java.io.IOException;

public interface NewJanusMessageInterface {
    void onNewMessage(JanusResponse message) throws JSONException, IOException;
}
