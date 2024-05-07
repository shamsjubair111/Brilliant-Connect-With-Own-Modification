package com.codewithkael.webrtcprojectforrecord;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithkael.webrtcprojectforrecord.models.JanusMessage;
import com.codewithkael.webrtcprojectforrecord.models.MessageModel;
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface;
import com.codewithkael.webrtcprojectforrecord.utils.SDPParser;

import org.webrtc.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RTCClient {

    private final Application application;
    private final String username;
    private final Websocket websocket;
    private final PeerConnection.Observer observer;

    private EglBase eglContext;
    private PeerConnectionFactory peerConnectionFactory;
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private PeerConnection peerConnection;
    private CameraVideoCapturer videoCapturer;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;

    public RTCClient(Application application, String username, Websocket websocket, PeerConnection.Observer observer) {
        this.application = application;
        this.username = username;
        this.websocket = websocket;
        this.observer = observer;

        initialize();
    }

    private void initialize() {
        eglContext = EglBase.create();
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(application)
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(options);



//        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
//        iceServers.add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer());
//        iceServers.add(PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer());
//        iceServers.add(PeerConnection.IceServer.builder("stun:stun.cloudflare.com:3478").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.voip.eutelia.it:3478").createIceServer());
//        iceServers.add(PeerConnection.IceServer.builder("stun:ip-9-232.sn2.clouditalia.com:3478").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").setUsername("83eebabf8b4cce9d5dbcb649").setPassword("2D7JvfkOQtBdYW3R").createIceServer());


        peerConnectionFactory = createPeerConnectionFactory();
        peerConnection = createPeerConnection(observer);
    }

    private PeerConnectionFactory createPeerConnectionFactory() {
        return PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglContext.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglContext.getEglBaseContext()))
                .createPeerConnectionFactory();
    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        return peerConnectionFactory.createPeerConnection(iceServers, observer);
    }

    public void initializeSurfaceView(SurfaceViewRenderer surface) {
        surface.setEnableHardwareScaler(true);
        surface.setMirror(true);
        surface.init(eglContext.getEglBaseContext(), null);
    }

    public void startLocalVideo(SurfaceViewRenderer surface) {
        VideoSource localVideoSource = peerConnectionFactory.createVideoSource(false);

        // Initialize localAudioSource
        MediaConstraints mediaConstraints = new MediaConstraints();
        AudioSource localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglContext.getEglBaseContext());

        videoCapturer = getVideoCapturer(application);
        if (videoCapturer != null) {
            videoCapturer.initialize(surfaceTextureHelper, surface.getContext(), localVideoSource.getCapturerObserver());
            videoCapturer.startCapture(320, 240, 30);
        }

        localVideoTrack = peerConnectionFactory.createVideoTrack("local_track", localVideoSource);
        if (localVideoTrack != null) {
            localVideoTrack.addSink(surface);
        }

        localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource);
        if (localAudioTrack != null) {
            MediaStream localStream = peerConnectionFactory.createLocalMediaStream("local_stream");
            localStream.addTrack(localAudioTrack);
//            localStream.addTrack(localVideoTrack);

            if (peerConnection != null) {
                peerConnection.addStream(localStream);
            }
        }
    }
    public void startLocalAudio() {

        // Initialize localAudioSource
        MediaConstraints mediaConstraints = new MediaConstraints();
        AudioSource localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource);
        if (localAudioTrack != null) {
            MediaStream localStream = peerConnectionFactory.createLocalMediaStream("local_stream");
            localStream.addTrack(localAudioTrack);
            if (peerConnection != null) {
                peerConnection.addStream(localStream);
            }
        }
    }

    private CameraVideoCapturer getVideoCapturer(Application application) {
        if (Camera2Enumerator.isSupported(application)) {
            // If Camera2 is supported, create a Camera2Enumerator and use it to create a CameraCapturer
            Camera2Enumerator enumerator = new Camera2Enumerator(application);
            String[] deviceNames = enumerator.getDeviceNames();
            if (deviceNames.length > 0) {
                // Use the first device
                return enumerator.createCapturer(deviceNames[0], null);
            } else {
                // Handle case when no cameras are available
                return null;
            }
        } else {
            // If Camera2 is not supported, fallback to Camera1Enumerator
            Camera1Enumerator enumerator = new Camera1Enumerator(true);
            String[] deviceNames = enumerator.getDeviceNames();
            if (deviceNames.length > 0) {
                // Use the first device
                return enumerator.createCapturer(deviceNames[0], null);
            } else {
                // Handle case when no cameras are available
                return null;
            }
        }
    }
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


    public void call(String target,long handleId,long sessionId) {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));

        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        System.out.println(sessionDescription.description);
                    }

                    @Override
                    public void onSetSuccess() {
                        String sdp = sessionDescription.description;
                        String type = sessionDescription.type.toString().toLowerCase();
                        JanusMessage.Body body;
                        if (websocket.dynamicClassInstance instanceof OutgoingCall)
                        {
                            sdp = sdp.replaceAll("(\\r)", "");
                            sdp = SDPParser.filterCodecs(sdp);

                            body = new JanusMessage.Body("call", target,false);
                        }
                        else
                        {
                            body = new JanusMessage.Body("call", target);
                        }

                        JanusMessage.Jsep jsep = new JanusMessage.Jsep(type, sdp);
                        JanusMessage message = new JanusMessage("message", body, TID(), jsep, sessionId, handleId);


                        // Get final JSON
                        String callMessageToJanus = null;
                        try {
                            callMessageToJanus = message.toJson(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(callMessageToJanus);
//                        callMessageToJanus = "{ \"janus\": \"message\", \"body\": { \"request\": \"call\", \"uri\": \"sip:2002@192.168.0.105\", \"autoaccept_reinvites\": false }, \"transaction\": \"56J4HXZcjPpU\", \"jsep\": { \"type\": \"offer\", \"sdp\": \"v=0\\r\\no=- 804709356943256984 2 IN IP4 127.0.0.1\\r\\ns=-\\r\\nt=0 0\\r\\na=group:BUNDLE 0\\r\\na=extmap-allow-mixed\\r\\na=msid-semantic: WMS de52d32f-97c5-42d2-885a-56207354f5fd\\r\\nm=audio 9 UDP/TLS/RTP/SAVPF 111 63 9 0 8 13 110 126\\r\\nc=IN IP4 0.0.0.0\\r\\na=rtcp:9 IN IP4 0.0.0.0\\r\\na=ice-ufrag:QMDB\\r\\na=ice-pwd:gc12jERQgZdU+uz1tPnQzwRG\\r\\na=ice-options:trickle\\r\\na=fingerprint:sha-256 4B:7C:AE:70:09:F6:06:99:12:78:B2:D0:8E:C6:0C:86:E0:39:B9:5B:0E:69:9E:39:8F:19:4F:A7:55:88:96:6B\\r\\na=setup:actpass\\r\\na=mid:0\\r\\na=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\\r\\na=extmap:2 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\\r\\na=extmap:3 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01\\r\\na=extmap:4 urn:ietf:params:rtp-hdrext:sdes:mid\\r\\na=sendrecv\\r\\na=msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\na=rtcp-mux\\r\\na=rtpmap:111 opus/48000/2\\r\\na=rtcp-fb:111 transport-cc\\r\\na=fmtp:111 minptime=10;useinbandfec=1\\r\\na=rtpmap:63 red/48000/2\\r\\na=fmtp:63 111/111\\r\\na=rtpmap:9 G722/8000\\r\\na=rtpmap:0 PCMU/8000\\r\\na=rtpmap:8 PCMA/8000\\r\\na=rtpmap:13 CN/8000\\r\\na=rtpmap:110 telephone-event/48000\\r\\na=rtpmap:126 telephone-event/8000\\r\\na=ssrc:291203786 cname:hKNnjFzhAYyMftEr\\r\\na=ssrc:291203786 msid:de52d32f-97c5-42d2-885a-56207354f5fd abc774c3-fa5f-4259-96ae-47d2b2f6ed92\\r\\n\" }, \"session_id\": " + sessionId + ", \"handle_id\": " + handleId + " }";
                        websocket.sendMessage(callMessageToJanus);
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        System.out.println("---------------------------------------"+s+"----------------------------------");
                    }

                    @Override
                    public void onSetFailure(String s) {
                        System.out.println("---------------------------------------"+s+"----------------------------------");
                    }
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {
                System.out.println("--------------------------Set succecc----------------------------");
            }

            @Override
            public void onCreateFailure(String s) {
                System.out.println("---------------------------------------"+s+"----------------------------------");
            }

            @Override
            public void onSetFailure(String s) {
                System.out.println("---------------------------------------"+s+"----------------------------------");
            }
        }, constraints);
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription) {
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {}

            @Override
            public void onSetSuccess() {}

            @Override
            public void onCreateFailure(String s) {}

            @Override
            public void onSetFailure(String s) {}
        }, sessionDescription);
    }

    public void answer(long sessionId, long handleId) {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));

        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {}

                    @Override
                    public void onSetSuccess() {

                        JanusMessage.Body body = new JanusMessage.Body("update");
                        JanusMessage.Jsep jsep = new JanusMessage.Jsep("answer", sessionDescription.description);
                        JanusMessage message = new JanusMessage("message", body, TID(), jsep, sessionId, handleId);
                        try {
                            websocket.sendMessage(message.toJson(message));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onCreateFailure(String s) {}

                    @Override
                    public void onSetFailure(String s) {}
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {}

            @Override
            public void onCreateFailure(String s) {}

            @Override
            public void onSetFailure(String s) {}
        }, constraints);
    }
    public void addIceCandidate(IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleAudio(boolean mute) {
        localAudioTrack.setEnabled(!mute);
    }

    public void toggleCamera(boolean cameraPause) {
        localVideoTrack.setEnabled(!cameraPause);
    }

    public void endCall() {
        peerConnection.close();
        iceServers.clear();
        peerConnection.dispose();
    }
}
