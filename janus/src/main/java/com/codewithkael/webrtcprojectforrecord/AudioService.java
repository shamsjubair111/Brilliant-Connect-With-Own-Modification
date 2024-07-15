package com.codewithkael.webrtcprojectforrecord;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import java.io.IOException;

public class AudioService extends Service {
    private MediaRecorder mediaRecorder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
