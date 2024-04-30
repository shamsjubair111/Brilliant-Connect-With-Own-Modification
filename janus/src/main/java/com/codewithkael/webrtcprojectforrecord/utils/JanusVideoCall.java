package com.codewithkael.webrtcprojectforrecord.utils;


import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithkael.webrtcprojectforrecord.R;
import com.codewithkael.webrtcprojectforrecord.RTCClient;
import com.codewithkael.webrtcprojectforrecord.Websocket;
import com.codewithkael.webrtcprojectforrecord.databinding.ActivityCallBinding;
import com.codewithkael.webrtcprojectforrecord.models.JanusCallHandlerInterface;
import com.codewithkael.webrtcprojectforrecord.models.JanusMessage;
import com.codewithkael.webrtcprojectforrecord.models.JanusResponse;
import com.google.gson.Gson;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.util.Random;

public class JanusVideoCall extends AppCompatActivity implements JanusCallHandlerInterface {
    private Websocket websocket;
    public static long sessionId = 0;
    public long handleId = 0;
    private int step = -1;

    private ActivityCallBinding binding;
    private String userName;
    private String receiver;
    private RTCClient rtcClient;
    private String TAG = "OutgoingCall";
    private String target = "";
    private Gson gson = new Gson();
    private boolean isMute = false ;
    private boolean isCameraPause = false;
    private RTCAudioManager rtcAudioManager;
    private boolean isSpeakerMode = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCallLayoutVisible();
        PermissionX.init(JanusVideoCall.this)
                .permissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                ).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        init();
                    } else {
                        Toast.makeText(JanusVideoCall.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void init() {
        userName = "sip:9638000123@103.248.13.73";
        receiver = "sip:"+getIntent().getStringExtra("callee")+"@103.248.13.73";
        websocket = new Websocket( this, JanusVideoCall.this);
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
//        binding.callBtn.setOnClickListener(v -> {
//        startCall(userName);
        target = receiver;
//        });

//        binding.switchCameraButton.setOnClickListener(v -> rtcClient.switchCamera());

        binding.micButton.setOnClickListener(v -> {
            isMute = !isMute;
            if (isMute) {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                binding.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            rtcClient.toggleAudio(isMute);
        });

//        binding.videoButton.setOnClickListener(v -> {
//            isCameraPause = !isCameraPause;
//            if (isCameraPause) {
//                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
//            } else {
//                binding.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
//            }
//            rtcClient.toggleCamera(isCameraPause);
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
            setCallLayoutGone();
//            setWhoToCallLayoutVisible();
//            setIncomingCallLayoutGone();
            rtcClient.endCall();
            hangup();
            finish();
//            try {
//                Intent intent = new Intent(OutgoingCall.this, Class.forName(getIntent().getStringExtra("activityName")));
//                startActivity(intent);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
        });
    }
    private void setIncomingCallLayoutGone() {
        binding.incomingCallLayout.setVisibility(View.GONE);
    }

    private void setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.setVisibility(View.VISIBLE);
    }

    private void setCallLayoutGone() {
        binding.callLayout.setVisibility(View.GONE);
    }

    private void setCallLayoutVisible() {
        binding.callLayout.setVisibility(View.VISIBLE);
    }

//    private void setWhoToCallLayoutGone() {
//        binding.whoToCallLayout.setVisibility(View.GONE);
//    }

    //    private void setWhoToCallLayoutVisible() {
//        binding.whoToCallLayout.setVisibility(View.VISIBLE);
//    }
    public static String TID()
    {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int length = 12;
        Random random = new Random();
        String transactionID = new String();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            transactionID+=(characters.charAt(index));
        }
        return transactionID;
    }
    @Override
    public void hangup()
    {
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
        String attachMessage = "{\"janus\":\"attach\",\"plugin\":\"" + pluginName + "\",\"opaque_id\":\"" + "siptest-" + TID() + "\",\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + "}";
        websocket.sendMessage(attachMessage);
    }

    public void registerToSIP(String username, String authuser, String displayName, String secret, String proxy) {
        // Construct the JSON message for registering to SIP
        String registerMessage = "{\"janus\":\"message\",\"body\":{\"request\":\"register\",\"username\":\"" + username + "\",\"authuser\":\"" + authuser + "\",\"display_name\":\"" + displayName + "\",\"secret\":\"" + secret + "\",\"proxy\":\"" + proxy + "\"},\"transaction\":\"" + TID() + "\",\"session_id\":" + sessionId + ",\"handle_id\":" + handleId + "}";
        websocket.sendMessage(registerMessage);
    }

    public void sendKeepAlive() {
        // Construct the JSON message for sending keep alive
        String keepAliveMessage = "{ \"janus\": \"keepalive\", \"session_id\":" + sessionId + ", \"transaction\":\"" + TID() + "\" }";
        websocket.sendMessage(keepAliveMessage);
    }

    public void sendCandidate(String candidate, String sdpMid, int sdpMLineIndex, String transaction) {
        // Construct the JSON message for sending candidate
        String candidateMessage = "{\"janus\":\"trickle\",\"candidate\":{\"candidate\":\"" + candidate + "\",\"sdpMid\":\"" + sdpMid + "\",\"sdpMLineIndex\":" + sdpMLineIndex + "},\"transaction\":\"" + transaction + "\",\"session_id\":" + sessionId + ",\"handle_id\":" + handleId + "}";
        websocket.sendMessage(candidateMessage);
    }

    public void startCall(String receiver) {
        websocket.sendMessage("{ \"janus\": \"message\", \"body\": { \"request\": \"call\", \"uri\": " + receiver + ", \"autoaccept_reinvites\": false }, \"transaction\": " + TID() + ", \"jsep\": { \"type\": \"offer\", \"sdp\": \"v=0\\r\\no=- 804709356943256984 2 IN IP4 127.0.0.1\\r\\ns=-\\r\\nt=0 0\\r\\na=group:BUNDLE 0\\r\\na=extmap-allow-mixed\\r\\na=msid-semantic: WMS de52d32f-97c5-42d2-885a-56207354f5fd\\r\\nm=audio 9 UDP/TLS/RTP/SAVPF 111 63 9 0 8 13 110 126\\r\\nc=IN IP4 0.0.0.0\\r\\na=rtcp:9 IN IP4 0.0.0.0\\r\\na=ice-ufrag:QMDB\\r\\na=ice-pwd:gc12jERQgZdU+uz1tPnQzwRG\\r\\na=ice-options:trickle\\r\\na=fingerprint:sha-256 4B:7C:AE:70:09:F6:06:99:12:78:B2:D0:8E:C6:0C:86:E0:39:B9:5B:0E:69:9E:39:8F:19:4F:A7:55:88:96:6B\\r\\na=setup:actpass\\r\\na=mid:0\\r\\na=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\\r\\na=extmap:2 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\\r\\na=extmap:3 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01\\r\\na=extmap:4 urn:ietf:params:rtp-hdrext:sdes:mid\\r\\na=sendrecv\\r\\na=msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\na=rtcp-mux\\r\\na=rtpmap:111 opus/48000/2\\r\\na=rtcp-fb:111 transport-cc\\r\\na=fmtp:111 minptime=10;useinbandfec=1\\r\\na=rtpmap:63 red/48000/2\\r\\na=fmtp:63 111/111\\r\\na=rtpmap:9 G722/8000\\r\\na=rtpmap:0 PCMU/8000\\r\\na=rtpmap:8 PCMA/8000\\r\\na=rtpmap:13 CN/8000\\r\\na=rtpmap:110 telephone-event/48000\\r\\na=rtpmap:126 telephone-event/8000\\r\\na=ssrc:291203786 cname:hKNnjFzhAYyMftEr\\r\\na=ssrc:291203786 msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\n\" }, \"session_id\": " + sessionId + ", \"handle_id\": " + handleId + " }");
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

    @Override
    public void onNewMessage(JanusResponse message) throws JSONException {
        String janusType = message.getJanus();
        switch (janusType) {
            case "keepalive":
                System.out.println("Got a keepalive on session " + sessionId);
                break;
            case "server_info":
            case "success":
                if(message.getSessionId() == 0)
                {
                    JanusResponse.Data = message.getData();
                    sessionId = JanusResponse.Data.getId();
                    attachPlugin("janus.plugin.sip");
                    System.out.println("Got a keepalive on session " + sessionId);
                }
                else
                {
                    JanusResponse.Data = message.getData();
                    handleId = JanusResponse.Data.getId();
//                    registerToSIP(userName, "2001", "2001", "2001", "sip:192.168.0.105:5060");
                    registerToSIP(userName, "9638000123", "9638000123", "telcobright@9638000123", "sip:103.248.13.73");
                }
                System.out.println("Session Running... ");
                break;
            case "timeout":
            {

                System.out.println("Time out....... ");
            }
            break;
            case "event":
                JanusResponse.plugin = message.getPluginData();
                if(JanusResponse.plugin.getData().getResult().getEvent().contains("registering"))
                {
                    System.out.println("Registering...");
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("registered"))
                {
                    System.out.println("Registered");
                    runOnUiThread(() -> {
//                        setWhoToCallLayoutGone();
                        setCallLayoutVisible();
                        rtcClient.startLocalAudio();
                        rtcClient.call(receiver,handleId,sessionId);
                    });


                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("calling"))
                {
                    System.out.println("Calling");
                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("ringing"))
                {
                    System.out.println("ringing");
                    //some works to do
                }
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("proceeding"))
                {
                    System.out.println("proceeding");
                    //some works to do
                }
//                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("accepted")
                else if(JanusResponse.plugin.getData().getResult().getEvent().contains("progress")
                )
                {
                    System.out.println("accepted");
                    if(message.getJsep().getSdp()!=null) {
                        JanusMessage.Jsep = message.getJsep();
                        SessionDescription session = new SessionDescription(
                                SessionDescription.Type.ANSWER, message.getJsep().getSdp());
                        rtcClient.onRemoteSessionReceived(session);
                        runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
                    }
                    //some works to do
                }
                else if (JanusResponse.plugin.getData().getResult().getEvent().contains("updating"))
                {
                    System.out.println("updating");
                    JanusMessage.Jsep = message.getJsep();
                    SessionDescription session = new SessionDescription(
                            SessionDescription.Type.OFFER, message.getJsep().getSdp());
                    rtcClient.onRemoteSessionReceived(session);
                    rtcClient.answer(sessionId, handleId);

                    runOnUiThread(() -> binding.remoteViewLoading.setVisibility(View.GONE));
                }
                else
                {
                    System.out.println("Some errors occur!");
                }
                break;
            case "webrtcup":
                System.out.println("webrtcup");
                break;
            case "media":

                System.out.println("media received");
                break;
            case "hangup":
                System.out.println(message.toString());
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

}
