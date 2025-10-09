package com.ubtrobot.mini.sdkdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.mini.sdkdemo.webrtc.*;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class RobotWebRTCActivity extends AppCompatActivity {

    private WebRTCManager rtcManager;
    private static final String webSocketUrl = BuildConfig.API_WEBSOCKET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_webrtc);

        SystemHandler systemHandler = SystemHandler.get();
        String robotSerial = systemHandler.getSerialNumber();
        final String SIGNALING_URL = webSocketUrl + "/signaling/" + robotSerial + "/robot";

        SurfaceViewRenderer localView = findViewById(R.id.localView);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        rtcManager = new WebRTCManager(this, localView, new WebRTCManager.Callback() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("type", "ice");
                    JSONObject cand = new JSONObject();
                    cand.put("sdpMid", candidate.sdpMid);
                    cand.put("sdpMLineIndex", candidate.sdpMLineIndex);
                    cand.put("candidate", candidate.sdp);
                    msg.put("candidate", cand);
                } catch (Exception e) { e.printStackTrace(); }
                SignalingSocketManager.getInstance(SIGNALING_URL).send(msg.toString());
            }

            @Override
            public void onLocalDescription(SessionDescription sdp) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("type", sdp.type.canonicalForm());
                    msg.put("sdp", sdp.description);
                } catch (Exception e) { e.printStackTrace(); }
                SignalingSocketManager.getInstance(SIGNALING_URL).send(msg.toString());
            }
        });

        SignalingSocketManager signaling = SignalingSocketManager.getInstance(SIGNALING_URL);
        signaling.setListener(new SignalingSocketManager.Listener() {
            @Override public void onConnected() {}
            @Override public void onMessage(String text) {
                try {
                    JSONObject data = new JSONObject(text);
                    String type = data.getString("type");
                    if (type.equals("answer")) {
                        rtcManager.setRemoteDescription(new SessionDescription(
                                SessionDescription.Type.ANSWER, data.getString("sdp")));
                    } else if (type.equals("ice")) {
                        JSONObject c = data.getJSONObject("candidate");
                        rtcManager.addIceCandidate(new IceCandidate(
                                c.getString("sdpMid"),
                                c.getInt("sdpMLineIndex"),
                                c.getString("candidate")));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override public void onDisconnected() {}
            @Override public void onError(String error) { error.intern(); }
        });

        // Xử lý nút Start
        btnStart.setOnClickListener(v -> {
            rtcManager.startLocalMedia(this);
            rtcManager.createPeerConnection();
            signaling.connect();
            rtcManager.createOffer();
        });

        // Xử lý nút Stop
        btnStop.setOnClickListener(v -> {
            if (rtcManager != null) rtcManager.release();
            signaling.disconnect();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtcManager != null) rtcManager.release();
        SignalingSocketManager signaling = SignalingSocketManager.getInstance(webSocketUrl + "/signaling/" + SystemHandler.get().getSerialNumber() + "/robot");
        signaling.disconnect();
    }
}
