package com.codewithkael.webrtcprojectforrecord;


import static com.codewithkael.webrtcprojectforrecord.RTCClient.TID;
import static sdk.chat.core.dao.Keys.Type;
import static sdk.chat.core.push.AbstractPushHandler.Action;
import static sdk.chat.core.push.AbstractPushHandler.Body;
import static sdk.chat.core.push.AbstractPushHandler.EncryptedMessage;
import static sdk.chat.core.push.AbstractPushHandler.SenderId;
import static sdk.chat.core.push.AbstractPushHandler.SenderName;
import static sdk.chat.core.push.AbstractPushHandler.ThreadId;
import static sdk.chat.core.push.AbstractPushHandler.UserIds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithkael.webrtcprojectforrecord.databinding.ActivityCallBinding;
import com.codewithkael.webrtcprojectforrecord.models.JanusCallHandlerInterface;
import com.codewithkael.webrtcprojectforrecord.models.JanusMessage;
import com.codewithkael.webrtcprojectforrecord.models.JanusResponse;
import com.codewithkael.webrtcprojectforrecord.utils.PeerConnectionObserver;
import com.codewithkael.webrtcprojectforrecord.utils.RTCAudioManager;
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

import callHandler.TelcobrightCallMessage;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;

public class AppToAppAudio extends AppCompatActivity implements JanusCallHandlerInterface {
    private static Websocket websocket;
    public static long sessionId = 0;
    public static long handleId = 0;
    private int step = -1;

    private ActivityCallBinding binding;
    private String userName;
    private static String receiver;
    private static RTCClient rtcClient;
    private String TAG = "AppToAppAudio";
    private String target = "";
    private Gson gson = new Gson();
    private boolean isMute = false;
    private boolean isCameraPause = false;
    private RTCAudioManager rtcAudioManager;
    private boolean isSpeakerMode = false;

    private boolean isVideo = false;
    HashMap<String, Object> newMessage = new HashMap<>();

    private static String type;

    public static void onReceived() {
        rtcClient.startLocalAudio();
        rtcClient.call(receiver, handleId, sessionId, type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCallLayoutVisible();
        PermissionX.init(AppToAppAudio.this)
                .permissions(
                        Manifest.permission.RECORD_AUDIO
                ).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        init();
                        ChatSDK.callActivities.put("AppToAppAudio",this);
                    } else {
                        Toast.makeText(AppToAppAudio.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                    }
                });
        type = getIntent().getStringExtra("type");
        binding.switchCameraButton.setVisibility(View.GONE);
        binding.videoButton.setVisibility(View.GONE);
    }


    private void init() {
        userName = ChatSDK.currentUser().getName() + "@localhost";
        receiver = getIntent().getStringExtra("receiverNumber") + "@localhost";
        websocket = new Websocket(this, AppToAppAudio.this);
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
//        });


        binding.micButton.setOnClickListener(v -> {
            isMute = !isMute;
            if (isMute) {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            rtcClient.toggleAudio(isMute);
        });


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
                ChatSDK.callActivities.remove("AppToAppAudio");
                newMessage.put("type", -1);
                ChatSDK.push().sendPushNotification(newMessage);
                rtcClient.stopLocalAudio();
                rtcClient.endCall();
//                setCallLayoutGone();
                hangup();
                finish();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }


        });
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

                    String receiverNumber = getIntent().getStringExtra("receiverNumber");
                    String roomName = null;
                    roomName = ChatSDK.currentUser().getName();

                    String threadEntityID = receiverNumber + "@localhost";
                    String senderId = ChatSDK.currentUser().getName() + "@localhost";
                    HashMap<String, HashMap<String, String>> userIds = new HashMap<String, HashMap<String, String>>();
                    HashMap<String, String> users = new HashMap<String, String>();
                    users.put(threadEntityID, receiverNumber);
                    userIds.put("userIds", users);
                    String action = "co.chatsdk.QuickReply";
                    String body = "video call";
                    int callType = 100;
                    users.put(ThreadId, senderId);
                    newMessage.put(ThreadId, threadEntityID);
                    newMessage.put(SenderName, ChatSDK.currentUser().getName());
                    newMessage.put(SenderId, senderId);
                    newMessage.put(UserIds, users);
                    newMessage.put(Action, action);
                    newMessage.put(Body, body);
                    newMessage.put(Type, callType);
                    ChatSDK.push().sendPushNotification(newMessage);

                    System.out.println("Registered");
//                    runOnUiThread(() -> {
//                        websocket.showToast("Registered Success");
//                    });
                    runOnUiThread(() -> {
//                        setWhoToCallLayoutGone();
//                        setCallLayoutVisible();
//                        websocket.showToast("Registered Success");
                        websocket.showToast("Calling");
//                        rtcClient.startLocalAudio();
//                        rtcClient.call(receiver,handleId,sessionId);
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
                        runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
                    }
                    //some works to do
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("updating")) {
                    System.out.println("updating");
                    JanusMessage.Jsep = message.getJsep();
                    SessionDescription session = new SessionDescription(
                            SessionDescription.Type.OFFER, message.getJsep().getSdp());
                    rtcClient.onRemoteSessionReceived(session);
                    rtcClient.answer(sessionId, handleId);

                    runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
                } else if (JanusResponse.plugin.getData().getResult().getEvent().contains("accepted")) {
                    if (message.getJsep().getSdp() != null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);
                        runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
                    }
                } else {
                    System.out.println("Some errors occur!");
                }
                break;
            case "webrtcup":
                System.out.println("webrtcup");
                websocket.showToast("webrtcup");
                break;
            case "media":

                System.out.println("media received");
                break;
            case "hangup":
                rtcClient.stopLocalAudio();
                finish();
//                finishAffinity();
//                handleHangup(json);
                break;
            case "ack":
                System.out.println(message.toString());
                break;
//            case "detached":
//                handleDetached(json);
//                break;

//            case "slowlink":
//                handleSlowLink(json);
//                break;
//            case "error":
//                handleError(json);
//                break;

//            case "timeout":
//                handleTimeout(json);
//                break;
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
        Toast.makeText(AppToAppAudio.this, "Call in progress", Toast.LENGTH_SHORT).show();

        // super.onBackPressed(); // Comment this super call to avoid calling finish() or fragmentmanager's backstack pop operation.
    }
    @Override
    public void finish() {
        super.finish();
        websocket.stopKeepAliveTimer();
//        websocket.showToast("hangup");
        websocket.closeSocket();
//        rtcClient.stopLocalAudio();
    }
}
