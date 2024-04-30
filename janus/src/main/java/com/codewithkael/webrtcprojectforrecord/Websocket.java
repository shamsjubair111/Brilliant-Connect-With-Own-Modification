package com.codewithkael.webrtcprojectforrecord;

import static com.codewithkael.webrtcprojectforrecord.OutgoingCall.TID;

import static org.webrtc.ContextUtils.getApplicationContext;
import static java.security.AccessController.getContext;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.codewithkael.webrtcprojectforrecord.models.JanusResponse;
import com.codewithkael.webrtcprojectforrecord.models.MessageModel;
import com.codewithkael.webrtcprojectforrecord.utils.JanusVideoCall;
import com.codewithkael.webrtcprojectforrecord.utils.NewJanusMessageInterface;
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Websocket {
    public WebSocketClient webSocket;
    private String userName;
    private final String TAG = "SocketRepository";
    private final Gson gson = new Gson();
    private final NewJanusMessageInterface messageInterface;
    public OutgoingCall outgoingCall = new OutgoingCall();
    public JanusVideoCall janusVideoCall = new JanusVideoCall();

    private enum callType {
        audio,
        video,
        sip
    }
    callType callType;
    public Websocket(NewJanusMessageInterface messageInterface, OutgoingCall outgoingCall) {
        this.messageInterface = messageInterface;
        this.outgoingCall = outgoingCall;
        this.callType = callType.sip;
    }
    public Websocket(NewJanusMessageInterface messageInterface, JanusVideoCall janusVideoCall) {
        this.messageInterface = messageInterface;
        this.janusVideoCall = janusVideoCall;
        this.callType = callType.video;
    }

    public void sendKeepAlive() {
        // Construct the JSON message for sending keep alive
        String keepAliveMessage = "{ \"janus\": \"keepalive\", \"session_id\":" + OutgoingCall.sessionId + ", \"transaction\":\"" + TID() + "\" }";
        sendMessage(keepAliveMessage);
    }

    public void initSocket(String username) {
        try {

            Map<String,String> httpHeaders  = new HashMap<>();
            httpHeaders.put("Sec-Websocket-Protocol","janus-protocol");

//            webSocket = new WebSocketClient(new URI("wss://tb.intercloud.com.bd/"),httpHeaders) {
            webSocket = new WebSocketClient(new URI("wss://103.248.13.73/"),httpHeaders) {
//            webSocket = new WebSocketClient(new URI("wss://192.168.0.105/"),httpHeaders) {
//            webSocket = new WebSocketClient(new URI("wss://192.168.68.122/"),httpHeaders) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("open");
//                    sendMessage("{\"janus\":\"create\",\"transaction\":\"" + TID() + "\"}");
                    outgoingCall.createSession();
                }

                @Override
                public void onMessage(String message) {
                    try {

                            messageInterface.onNewMessage(gson.fromJson(message, JanusResponse.class));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: " + reason);
                    stopKeepAliveTimer();
                }

                @Override
                public void onError(Exception ex) {
                    Log.d(TAG, "onError: " + ex);
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (webSocket != null) {
            webSocket.connect();
            // Set a timer task to manage connection timeout
            TimerTask connectionTimeoutTask = new TimerTask() {
                @Override
                public void run() {
                    if (!webSocket.isOpen()) {
                        System.out.println("Service Unavailable");
                        showServiceUnavailableToast();
                        outgoingCall.finish();
                        // WebSocket connection failed due to timeout
                        // Handle the timeout here
                        // For example, show an error message or retry the connection
                    }
                }
            };
            Timer connectionTimer = new Timer();
            connectionTimer.schedule(connectionTimeoutTask, 2000); // 2 seconds timeout

//            outgoingCall.createSession();
        }
    }

    Timer timer = new Timer("Timer");
    public void startKeepAliveTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                sendKeepAlive();
            }
        };

        long delay = 1000L;
        long interval = 25000L;
        timer.schedule(task, delay, interval);
    }

    public void stopKeepAliveTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
    public void closeSocket() {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.close();
        }
    }

    public void showServiceUnavailableToast() {
        outgoingCall.runOnUiThread(() -> {
            Toast.makeText(outgoingCall, "Service Unavailable", Toast.LENGTH_SHORT).show();
        });
    }
    public void showToast(String message) {
        outgoingCall.runOnUiThread(() -> {
            Toast.makeText(outgoingCall, message, Toast.LENGTH_SHORT).show();
        });
    }

    public void sendMessage(String message) {
        try {
            Log.d(TAG, "sendMessageToSocket: " + message);
            if (webSocket != null) {
                webSocket.send(message);
            }
        } catch (Exception e) {
            Log.d(TAG, "sendMessageToSocket: " + e);
        }
    }
}
