package com.codewithkael.webrtcprojectforrecord;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.codewithkael.webrtcprojectforrecord.models.JanusMessage;
import com.codewithkael.webrtcprojectforrecord.utils.SDPParser;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private MediaStream localStream;
    private String type;
    private SurfaceTextureHelper surfaceTextureHelper;
    private SurfaceViewRenderer surfaceViewRenderer;

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
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.cloudflare.com:3478").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.voip.eutelia.it:3478").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:ip-9-232.sn2.clouditalia.com:3478").createIceServer());
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

    private void configureVideoEncoder(VideoSource videoSource) {
        // Set additional encoder settings if needed
        // For example, configure bitrate and resolution constraints
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", "720"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", "1280"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", "30"));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", "30"));

        videoSource.adaptOutputFormat(1280, 720, 30); // Set the desired resolution and frame rate
    }


    private void cleanUpSurfaceViewRenderer(SurfaceViewRenderer surface) {

        surface.release();
        surface.clearImage();
    }

    public void initializeSurfaceView(SurfaceViewRenderer surfaceViewRenderer) {
        this.surfaceViewRenderer = surfaceViewRenderer;
        // Ensure this code runs on the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // The UI operation
                surfaceViewRenderer.setEnableHardwareScaler(true);
//                surfaceViewRenderer.setMirror(true);
                surfaceViewRenderer.init(eglContext.getEglBaseContext(), null);
            }
        });
    }

//    public void startLocalVideo(SurfaceViewRenderer surface) {
//        VideoSource localVideoSource = peerConnectionFactory.createVideoSource(false);
//
//        // Initialize localAudioSource
//        MediaConstraints mediaConstraints = new MediaConstraints();
//        AudioSource localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
//        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(
//                Thread.currentThread().getName(), eglContext.getEglBaseContext());
//
//        videoCapturer = getVideoCapturer(application);
//        if (videoCapturer != null) {
//            videoCapturer.initialize(surfaceTextureHelper, surface.getContext(), localVideoSource.getCapturerObserver());
//            videoCapturer.startCapture(320, 240, 30);
//        }
//
//        localVideoTrack = peerConnectionFactory.createVideoTrack("local_track", localVideoSource);
//        if (localVideoTrack != null) {
//            localVideoTrack.addSink(surface);
//        }
//
//        localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource);
//        if (localAudioTrack != null) {
//            MediaStream localStream = peerConnectionFactory.createLocalMediaStream("local_stream");
//            localStream.addTrack(localAudioTrack);
//            localStream.addTrack(localVideoTrack);
//
//            if (peerConnection != null) {
//                peerConnection.addStream(localStream);
//            }
//        }
//    }


    public void startLocalVideo(SurfaceViewRenderer surface) {
        if (surface != null) {
            cleanUpSurfaceViewRenderer(surface);
        }
        VideoSource localVideoSource = peerConnectionFactory.createVideoSource(false);

        // Configure the video encoder for better video quality
        configureVideoEncoder(localVideoSource);

        // Initialize localAudioSource
        MediaConstraints mediaConstraints = new MediaConstraints();
        AudioSource localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName(), eglContext.getEglBaseContext());

        videoCapturer = getVideoCapturer(application);
        if (videoCapturer != null) {
            videoCapturer.initialize(surfaceTextureHelper, surface.getContext(), localVideoSource.getCapturerObserver());

            // Set higher resolution and frame rate for better video quality
            videoCapturer.startCapture(1280, 720, 30); // 720p at 30fps
        }

        localVideoTrack = peerConnectionFactory.createVideoTrack("local_track", localVideoSource);
        if (localVideoTrack != null) {
            localVideoTrack.addSink(surface);
        }

        localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource);
        if (localAudioTrack != null) {
            localStream = peerConnectionFactory.createLocalMediaStream("local_stream");
            localStream.addTrack(localAudioTrack);
            localStream.addTrack(localVideoTrack);

            if (peerConnection != null) {
                peerConnection.addStream(localStream);
            }
        }
    }

    public void stopLocalVideo() {
        if (localVideoTrack != null) {
            localVideoTrack.setEnabled(false); // Disable the track
            localVideoTrack.dispose(); // Dispose the track
            localVideoTrack = null;
        }
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        if (peerConnection != null && localStream != null) {
            // Remove the local stream from the peer connection
            peerConnection.removeStream(localStream);
            localStream = null; // Reset localStream reference
        }
        if (this.surfaceViewRenderer != null) {
            cleanUpSurfaceViewRenderer(this.surfaceViewRenderer);

        }
    }

    public void startLocalAudio() {

        // Initialize localAudioSource
        MediaConstraints mediaConstraints = new MediaConstraints();
        AudioSource localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_track_audio", localAudioSource);
        if (localAudioTrack != null) {
            localStream = peerConnectionFactory.createLocalMediaStream("local_stream");
            localStream.addTrack(localAudioTrack);
            if (peerConnection != null) {
                peerConnection.addStream(localStream);
            }
        }
    }

    public void stopLocalAudio() {
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(false); // Disable the track
            localAudioTrack.dispose(); // Dispose the track
            localAudioTrack = null;
        }
        if (peerConnection != null && localStream != null) {
            // Remove the local stream from the peer connection
            peerConnection.removeStream(localStream);
        }
    }

    public void stopLocalMedia() {
        stopLocalAudio();
        stopLocalVideo();
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

    public static String modifySdp(String sdp) {
        // Pattern to match the exact fmtp line for Opus
        String opusFmtpPattern = "a=fmtp:111 minptime=10;useinbandfec=1";
        String newFmtp = "a=fmtp:111 minptime=10;useinbandfec=1;maxaveragebitrate=16000";

        // Replace the old fmtp line with the new one
        String modifiedSdp = sdp.replace(opusFmtpPattern, newFmtp);

        return modifiedSdp;
    }

    public void call(String target, long handleId, long sessionId, String type) {
        MediaConstraints constraints = new MediaConstraints();
        this.type = type;
        if (type.equals("audio")) {
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        } else {
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        }


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
//                        sdp = sdp.replace("opus/48000/2", "opus/16000/2");
                        if (websocket.dynamicClassInstance instanceof OutgoingCall) {
                            sdp = sdp.replaceAll("(\\r)", "");
                            sdp = SDPParser.filterCodecs(sdp);
                            body = new JanusMessage.Body("call", target, false);
                        } else if (websocket.dynamicClassInstance instanceof AppToAppAudio) {
                            sdp = sdp.replaceAll("(\\r)", "");
                            sdp = SDPParser.filterCodecs(sdp);
                            sdp = modifySdp(sdp);
                            body = new JanusMessage.Body("call", target);
                        } else {
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
                        System.out.println("---------------------------------------" + s + "----------------------------------");
                    }

                    @Override
                    public void onSetFailure(String s) {
                        System.out.println("---------------------------------------" + s + "----------------------------------");
                    }
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {
                System.out.println("--------------------------Set succecc----------------------------");
            }

            @Override
            public void onCreateFailure(String s) {
                System.out.println("---------------------------------------" + s + "----------------------------------");
            }

            @Override
            public void onSetFailure(String s) {
                System.out.println("---------------------------------------" + s + "----------------------------------");
            }
        }, constraints);
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription) {
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
            }

            @Override
            public void onSetSuccess() {
            }

            @Override
            public void onCreateFailure(String s) {
            }

            @Override
            public void onSetFailure(String s) {
            }
        }, sessionDescription);
    }


    public void answer(long sessionId, long handleId) {

        MediaConstraints constraints = new MediaConstraints();
        this.type = ReceiverActivityAudio.getType();
        if (type.equals("audio")) {
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        } else {
            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        }

        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        System.out.println(sessionDescription);
                    }

                    @Override
                    public void onSetSuccess() {
                        String sdp = sessionDescription.description;
                        String type = sessionDescription.type.toString().toLowerCase();
                        JanusMessage.Body body = new JanusMessage.Body("accept");
                        JanusMessage.Jsep jsep = new JanusMessage.Jsep(type, sdp);
                        JanusMessage message = new JanusMessage("message", body, TID(), jsep, sessionId, handleId);
                        try {
                            websocket.sendMessage(message.toJson(message));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onSetFailure(String s) {
                        System.out.println(s);
                    }
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {
            }

            @Override
            public void onCreateFailure(String s) {
            }

            @Override
            public void onSetFailure(String s) {
            }
        }, constraints);
    }

    public void addIceCandidate(IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleAudio(boolean mute) {
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(!mute);

        }
    }

    public void toggleCamera(boolean cameraPause) {
        localVideoTrack.setEnabled(!cameraPause);
    }

    //    public void endCall() {
//        peerConnection.close();
//        iceServers.clear();
//        peerConnection.dispose();
//        // Stop all tracks on the local stream
//        try {
//
//                localStream.dispose();
//                localStream = null;
//
//        }
//        catch (Exception e)
//        {
//            System.out.println(e);
//        }
//
//    }
    public void endCall() {
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }
        if (!(websocket.dynamicClassInstance instanceof OutgoingCall) && localStream != null) {
            localStream.dispose();
            localStream = null;
        }
        iceServers.clear();
    }
}

