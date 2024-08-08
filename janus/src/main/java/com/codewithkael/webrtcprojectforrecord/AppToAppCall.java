package com.codewithkael.webrtcprojectforrecord;


import static sdk.chat.core.dao.Keys.Type;
import static sdk.chat.core.push.AbstractPushHandler.Action;
import static sdk.chat.core.push.AbstractPushHandler.Body;
import static sdk.chat.core.push.AbstractPushHandler.SenderId;
import static sdk.chat.core.push.AbstractPushHandler.SenderName;
import static sdk.chat.core.push.AbstractPushHandler.ThreadId;
import static sdk.chat.core.push.AbstractPushHandler.UserIds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.codewithkael.webrtcprojectforrecord.databinding.ActivityCallBinding;
import com.codewithkael.webrtcprojectforrecord.models.JanusCallHandlerInterface;
import com.codewithkael.webrtcprojectforrecord.models.JanusMessage;
import com.codewithkael.webrtcprojectforrecord.models.JanusResponse;
import com.codewithkael.webrtcprojectforrecord.utils.NumberStringFormater;
import com.codewithkael.webrtcprojectforrecord.utils.PeerConnectionObserver;
import com.codewithkael.webrtcprojectforrecord.utils.RTCAudioManager;
import com.codewithkael.webrtcprojectforrecord.utils.RTCClientSingleton;
import com.google.gson.Gson;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import sdk.chat.core.session.ChatSDK;

import java.util.Timer;
import java.util.TimerTask;

public  class AppToAppCall extends AppCompatActivity implements JanusCallHandlerInterface {

    private static Timer timer;
    private static long startTime;
    private static Websocket websocket;
    public static long sessionId = 0;
    public static long handleId = 0;
    private int step = -1;
    private static boolean callInProgress = false;

    private ActivityCallBinding binding;
    private String userName;
    private static String receiver;
    private static String receiverNumber;
    private static RTCClient rtcClient;
    private String TAG = "AppToAppCall";
    private String target = "";
    private Gson gson = new Gson();
    private boolean isMute = false;
    private boolean isCameraPause = false;
    private RTCAudioManager rtcAudioManager;
    private boolean isSpeakerMode = false;
    SQLiteCallFragmentHelper sqLiteCallFragmentHelper;
    private boolean isVideo = false;
    HashMap<String, Object> newMessage = new HashMap<>();

    public static String type;

    private static final int PERMISSION_REQUEST_CODE = 1;

    public static void onReceived() {
        if(type.equals("audio")){
            rtcClient.startLocalAudio();
        }
        RTCClientSingleton.getInstance().setRtcClient(rtcClient);
        final Context context = ChatSDK.ctx();
        rtcClient.call(receiver, handleId, sessionId, type);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startForegroundService();
            }
        }
    }
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }
    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, AudioCallService.class);
        serviceIntent.putExtra("receiverNumber",receiverNumber);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, AudioCallService.class);
        stopService(serviceIntent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getStringExtra("type");
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY".equals(intent.getAction())) {
                ChatSDK.callActivities.remove("AppToAppCall");
                newMessage.put("type", -1);
                runOnUiThread(() -> {
                    ChatSDK.push().sendPushNotification(newMessage);
                    hangup();

                });
                if(type.contains("video")){
                    rtcClient.stopLocalMedia();

                }else {
                    rtcClient.stopLocalAudio();
                }
                rtcClient.endCall();
                callInProgress = false;
                finish();
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_CHANGE_SPEAKER".equals(intent.getAction()))
            {
                isSpeakerMode = !isSpeakerMode;
                if (isSpeakerMode) {
                    binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24);
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);
                } else {
                    binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24);
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
                }
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE".equals(intent.getAction()))
            {
                isMute = !isMute;
                if (isMute) {
                    binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                } else {
                    binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                }
                rtcClient.toggleAudio(isMute);
            }
            else if("com.codewithkael.webrtcprojectforrecord.ACTION_RESUME".equals(intent.getAction()))
            {
                onResume();
            }
        }
    };

    private void init() {
        callInProgress = true;
        // Set the flags to show the activity on the lock screen and turn the screen on
        IntentFilter filter = new IntentFilter("com.codewithkael.webrtcprojectforrecord.ACTION_FINISH_ACTIVITY");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_CHANGE_SPEAKER");
        filter.addAction("com.codewithkael.webrtcprojectforrecord.ACTION_MUTEACTION_ACTION_RESUME");
        registerReceiver(broadcastReceiver, filter);
//        ChatSDK.ctx().registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED);
        if(type.contains("audio"))
        {
            binding.videoButton.setVisibility(View.GONE);

        }
        binding.contactName.setText(getIntent().getStringExtra("contactName"));
        binding.contactNumber.setText(getIntent().getStringExtra("receiverNumber"));
        userName = ChatSDK.auth().getCurrentUserEntityID();       //ChatSDK.currentUser().getName() + "@localhost";
        receiver = NumberStringFormater.normalizePhoneNumber(getIntent().getStringExtra("receiverNumber")) + "@localhost";
        websocket = new Websocket(this, AppToAppCall.this);
        if (userName != null) {
            websocket.initSocket(userName);
        }

        rtcClient = new RTCClient(getApplication(), userName, websocket, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate p0) {
                super.onIceCandidate(p0);
                rtcClient.addIceCandidate(p0);
                if (p0 != null) {
                    String sdpMid = p0.sdpMid;
                    int sdpMLineIndex = p0.sdpMLineIndex;
                    String sdpCandidate = p0.sdp;
                    JanusMessage.Candidate candidate = new JanusMessage.Candidate(sdpCandidate, sdpMid, sdpMLineIndex);
                    JanusMessage candidateMessage = new JanusMessage("trickle", candidate, TID(), sessionId, handleId);
                    try {
                        String message = candidateMessage.toJson(candidateMessage);
                        websocket.sendMessage(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            @Override
            public void onAddStream(MediaStream p0) {
                super.onAddStream(p0);
                if (p0 != null && p0.videoTracks.size() > 0) {
                    p0.videoTracks.get(0).addSink(binding.remoteView);
                    Log.d(TAG, "onAddStream: " + p0);
                }
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                super.onIceGatheringChange(iceGatheringState);
                if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE) {
                    // ICE gathering is complete
                    JanusMessage.Candidate candidate = new JanusMessage.Candidate(true);
                    JanusMessage candidateMessage = new JanusMessage("trickle", candidate, TID(), sessionId, handleId);
                    try {
                        String message = candidateMessage.toJson(candidateMessage);
                        websocket.sendMessage(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // If ICE candidates arrive before this event, the complete trickle message will be sent there.
                    // If not, it will be sent when the first ICE candidate arrives.
                }
            }
        });
        rtcAudioManager = new RTCAudioManager(this);
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
        target = receiver;
        if(type.contains("video"))
        {
            rtcClient.initializeSurfaceView(binding.localView);
            rtcClient.initializeSurfaceView(binding.remoteView);
            rtcClient.startLocalVideo(binding.localView);

//        });

            binding.videoButton.setOnClickListener(v -> {
                isCameraPause = !isCameraPause;
                if (isCameraPause) {
                    binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                } else {
                    binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
                }
                rtcClient.toggleCamera(isCameraPause);
            });
        }

//        });



        binding.audioOutputButton.setOnClickListener(v -> {
            isSpeakerMode = !isSpeakerMode;
            if (isSpeakerMode) {
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24);
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                binding.audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24);
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE);
            }
        });


        binding.endCallButton.setOnClickListener(v -> {
            try {
//                finishWithSendNotfication();
                ChatSDK.callActivities.remove("AppToAppCall");
                newMessage.put("type", -1);
                ChatSDK.push().sendPushNotification(newMessage);
                rtcClient.stopLocalMedia();
                rtcClient.endCall();
                hangup();
                finish();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }


        });


    }

    private void finishWithSendNotfication() {
        ChatSDK.callActivities.remove("AppToAppCall");
        newMessage.put("type", -1);
        runOnUiThread(() -> {
            ChatSDK.push().sendPushNotification(newMessage);
            hangup();
        });
        try {
            stopForegroundService();
            rtcClient.stopLocalAudio();
            if(type.contains("video"))
            {
            rtcClient.stopLocalVideo();
            }
            rtcClient.endCall();
            finish();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

    }

    @Override
    public void onNewMessage(JanusResponse message) throws JSONException {
        String janusType = message.getJanus();
        switch (janusType) {
            case "keepalive":
                System.out.println("Got a keepalive on session " + sessionId);
                break;
            case "server_info":
            case "success":
                if (message.getSessionId() == 0) {
                    JanusResponse.Data = message.getData();
                    sessionId = JanusResponse.Data.getId();
                    attachPlugin("janus.plugin.videocall");
//                    websocket.showToast("Janus Connected");
                } else {
                    JanusResponse.Data = message.getData();
                    handleId = JanusResponse.Data.getId();
//                    registerToSIP(userName, "2001", "2001", "2001", "sip:192.168.0.105:5060");
                    register(userName);
                    websocket.startKeepAliveTimer();
                }
                System.out.println("Session Running... ");
                break;
            case "timeout": {
                System.out.println("Time out....... ");
                websocket.showToast("Time Out");

                finish();
            }
            break;
            case "event":
                JanusResponse.plugin = message.getPluginData();


                if (JanusResponse.plugin.getData().getErrorCode() == 476 || JanusResponse.plugin.getData().getResult().getEvent().contains("registered")) {

                    receiverNumber = NumberStringFormater.normalizePhoneNumber(getIntent().getStringExtra("receiverNumber"));
                    String roomName = null;
                    roomName = ChatSDK.auth().getCurrentUserEntityID().split("@")[0];   //ChatSDK.currentUser().getName();

                    String threadEntityID = receiverNumber + "@localhost";
                    String userThreadId = ChatSDK.auth().getCurrentUserEntityID();      //ChatSDK.currentUser().getName() + "@localhost";
                    HashMap<String, HashMap<String, String>> userIds = new HashMap<String, HashMap<String, String>>();
                    HashMap<String, String> users = new HashMap<String, String>();
                    users.put(threadEntityID, receiverNumber);
                    userIds.put("userIds", users);
                    String action = "co.chatsdk.QuickReply";
                    String body = "video call";
                    int callType;

                    if(type.contains("video"))
                    {
                        callType = 101;
                    }
                    else
                    {
                        callType = 100;
                    }

                    users.put(ThreadId, userThreadId);
                    newMessage.put(ThreadId, threadEntityID);
                    newMessage.put(SenderName, ChatSDK.auth().getCurrentUserEntityID().split("@")[0]);
                    newMessage.put(SenderId, userThreadId);
                    newMessage.put(UserIds, users);
                    newMessage.put(Action, action);
                    newMessage.put(Body, body);
                    newMessage.put(Type, callType);
                    runOnUiThread(() -> {
                        ChatSDK.push().sendPushNotification(newMessage);
                        websocket.showToast("Calling");

                    });

                }
                else if (JanusResponse.plugin.getData().getResult().getEvent().contains("registering")) {
                    System.out.println("Registering...");
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("calling")) {
                    System.out.println("Calling");

                    //some works to do
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("ringing")) {
                    System.out.println("ringing");
                    websocket.showToast("ringing");
                    //some works to do
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("proceeding")) {
                    System.out.println("proceeding");
                    websocket.showToast("proceeding");
                    //some works to do
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("registration_failed")) {

                    websocket.showToast("registration_failed");
                    System.out.println(message.toString());
                    websocket.stopKeepAliveTimer();
                    websocket.closeSocket();
                    finish();
                    //some works to do
                }

//                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("accepted")
                else if (JanusResponse.plugin.getData().getResult().getEvent().contains("progress")
                ) {
                    System.out.println("accepted");
                    if (message.getJsep().getSdp() != null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);

                    }
                    //some works to do
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("updating")) {
                    System.out.println("updating");
                    JanusMessage.Jsep = message.getJsep();
                    SessionDescription session = new SessionDescription(
                            SessionDescription.Type.OFFER, message.getJsep().getSdp());
                    rtcClient.onRemoteSessionReceived(session);
                    rtcClient.answer(sessionId, handleId);


                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("accepted")) {
                    if (message.getJsep().getSdp() != null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);

                    }
                } else {
                    System.out.println("Some errors occur!");
                }
                break;
            case "webrtcup":
                startTimer();
                if (checkPermissions()) {
                    startForegroundService();
                } else {
                    requestPermissions();
                }
                binding.micButton.setOnClickListener(v -> {
                    Intent changeNotificationIcon = new Intent("com.codewithkael.webrtcprojectforrecord.ACTION_MUTE_NOTIFICATION_ICON");
                    ChatSDK.ctx().sendBroadcast(changeNotificationIcon);
                    isMute = !isMute;
                    if (isMute) {
                        binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    } else {
                        binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
                    }
                    rtcClient.toggleAudio(isMute);
                });

                System.out.println("webrtcup");
                websocket.showToast("webrtcup");
                break;
            case "media":
                startTime = System.currentTimeMillis();
                System.out.println("media received");
                break;
            case "hangup":
                callInProgress =false;
                runOnUiThread(() -> {
                    rtcClient.stopLocalAudio();
                    stopForegroundService();
                    sqLiteCallFragmentHelper = new SQLiteCallFragmentHelper(this);
                    SQLiteDatabase sqLiteDatabase = sqLiteCallFragmentHelper.getWritableDatabase();
                    long rowId =  sqLiteCallFragmentHelper.insertData(getIntent().getStringExtra("contactName"), getIntent().getStringExtra("receiverNumber"));
                    if(rowId >0){
                        websocket.showToast("Data Inserted");
//                    Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
                    }
                });
                stopTimer();
                finish();
//                finishAffinity();
//                handleHangup(json);
                break;
            case "ack":
                System.out.println(message.toString());
                break;
            default:
                System.out.println("Unknown message/event  '" + janusType + "' on session " + sessionId);
                System.out.println(message.toString());
        }
    }


    private void setCallLayoutGone() {
        binding.callLayout.setVisibility(View.GONE);
    }

    private void setCallLayoutVisible() {
        binding.callLayout.setVisibility(View.VISIBLE);
    }

    public static String TID() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 12;
        Random random = new Random();
        String transactionID = new String();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            transactionID += (characters.charAt(index));
        }
        return transactionID;
    }

    @Override
    public void hangup() {
        JanusMessage.Body body = new JanusMessage.Body("hangup");
        JanusMessage message = new JanusMessage("message", body, TID(), sessionId, handleId);
        sqLiteCallFragmentHelper = new SQLiteCallFragmentHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteCallFragmentHelper.getWritableDatabase();
        long rowId =  sqLiteCallFragmentHelper.insertData(getIntent().getStringExtra("contactName"), getIntent().getStringExtra("receiverNumber"));
        if(rowId >0){
            websocket.showToast("Data Inserted");
//            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show();
        }
        stopTimer();
        try {
            websocket.sendMessage(message.toJson(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void createSession() {
        // Construct the JSON message for creating a session
        String createSessionMessage = "{\"janus\":\"create\",\"transaction\":\"" + TID() + "\"}";
        step = 1;
        websocket.sendMessage(createSessionMessage);
    }

    public void attachPlugin(String pluginName) {
        // Construct the JSON message for attaching to a plugin
        String attachMessage = "{\"janus\":\"attach\",\"plugin\":\"" + pluginName + "\",\"opaque_id\":\"" + "videocalltest-" + TID() + "\",\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + "}";
        websocket.sendMessage(attachMessage);
    }

    public void register(String username) {
        // Construct the JSON message for registering to SIP
        String registerMessage = "{\n" +
                "  \"janus\": \"message\",\n" +
                "  \"body\": {\n" +
                "    \"request\": \"register\",\n" +
                "    \"username\": \"" + username + "\"\n" +
                "  },\n" +
                " \"transaction\": \"" + TID() + "\",\n" +
                "  \"session_id\": " + sessionId + ",\n" +
                "  \"handle_id\": " + handleId + " \n" +
                "}";
        websocket.sendMessage(registerMessage);
    }

    @Override
    public void handleSentMessage(String message) {
        // Implement logic to handle sent messages
        System.out.println("Sent message: " + message);
    }

    @Override
    public void handleReceivedMessage(String message) {
        // Implement logic to handle received messages
        System.out.println("Received message: " + message);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Toast.makeText(AppToAppCall.this, "Call in progress", Toast.LENGTH_SHORT).show();

        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }
    @Override
    public void finish() {
        super.finish();
        stopForegroundService();
        websocket.stopKeepAliveTimer();
        websocket.closeSocket();
        callInProgress = false;
        if(type.contains("video"))
        {
            super.finishAndRemoveTask();

        }
    }


    public  void startTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("Call duration: " + duration / 1000 + " seconds");


                String formattedDuration = formatDuration(duration / 1000);
                runOnUiThread(() -> binding.callDuration.setText(formattedDuration));

            }
        }, 1000, 1000); // Start updating every second
    }

    public  void stopTimer() {
        timer.cancel();
        long duration = System.currentTimeMillis() - startTime;

    }


    private String formatDuration(long durationInSeconds) {
        long hours = durationInSeconds / 3600;
        long minutes = (durationInSeconds % 3600) / 60;
        long seconds = durationInSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        rtcClient.resumeLocalAudio();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!callInProgress) {
            timer = new Timer();
            if(type.equals("video")){
                PermissionX.init(AppToAppCall.this)
                        .permissions(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA
                        ).request((allGranted, grantedList, deniedList) -> {
                            if (allGranted) {
                                binding = ActivityCallBinding.inflate(getLayoutInflater());
                                ChatSDK.callActivities.put("AppToAppCall",this);
                                setContentView(binding.getRoot());
                                setCallLayoutVisible();
                                init();
                            } else {
                                Toast.makeText(AppToAppCall.this, "You should accept camera and audio permissions", Toast.LENGTH_LONG).show();
                                super.finish();
                            }
                        });
            }
            else {
                PermissionX.init(AppToAppCall.this)
                        .permissions(
                                Manifest.permission.RECORD_AUDIO
                        ).request((allGranted, grantedList, deniedList) -> {
                            if (allGranted) {
                                binding = ActivityCallBinding.inflate(getLayoutInflater());
                                setContentView(binding.getRoot());
                                setCallLayoutVisible();
                                ChatSDK.callActivities.put("AppToAppCall", this);
                                init();
                            } else {
                                Toast.makeText(AppToAppCall.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }
    }

}