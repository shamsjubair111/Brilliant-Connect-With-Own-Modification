package com.codewithkael.webrtcprojectforrecord.utils;

import com.codewithkael.webrtcprojectforrecord.RTCClient;

import org.webrtc.PeerConnection;

public class RTCClientSingleton {
    private static RTCClientSingleton instance;
    private RTCClient rtcClient;

    private RTCClientSingleton() {
    }

    public static RTCClientSingleton getInstance() {
        if (instance == null) {
            instance = new RTCClientSingleton();
        }
        return instance;
    }

    public RTCClient getRtcClient() {
        return rtcClient;
    }

    public void setRtcClient(RTCClient rtcClient) {
        this.rtcClient = rtcClient;
    }
}
