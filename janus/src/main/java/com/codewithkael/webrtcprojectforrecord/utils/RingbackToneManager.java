package com.codewithkael.webrtcprojectforrecord.utils;

import static com.codewithkael.webrtcprojectforrecord.R.raw.*;

import static sdk.chat.core.session.ChatSDK.shared;

import android.content.Context;
import android.media.MediaPlayer;

import com.codewithkael.webrtcprojectforrecord.R;

import sdk.chat.core.session.ChatSDK;

public class RingbackToneManager {
    private static RingbackToneManager instance;
    private  Context context;
    private MediaPlayer mediaPlayer;

    private RingbackToneManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized RingbackToneManager getInstance() {
        if (instance == null) {
            instance = new RingbackToneManager();
        }

        return instance;
    }

    public void startRingbackTone() {
        // Release any previous instance
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        // Initialize the MediaPlayer with the custom MP3 file
        mediaPlayer = MediaPlayer.create(shared().context(),R.raw.ringback);


        // Set looping if needed
        mediaPlayer.setLooping(true);

        // Start playing the ringback tone
        mediaPlayer.start();
    }

    public void stopRingbackTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
