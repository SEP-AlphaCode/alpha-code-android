package com.ubtrobot.mini.sdkdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.WebRTCHandler;
import com.ubtrobot.mini.sdkdemo.webrtc.*;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
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
    private SurfaceViewRenderer localView;
    private SignalingSocketManager signaling;
    private String SIGNALING_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_webrtc);

        SystemHandler systemHandler = SystemHandler.get();
        String robotSerial = systemHandler.getSerialNumber();
        SIGNALING_URL = webSocketUrl + "/signaling/" + robotSerial + "/robot";

        localView = findViewById(R.id.localView);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        setupSurfaceView();
        initializeWebRTC();

        // Register this activity with the WebRTCHandler
        WebRTCHandler.getInstance().setCurrentActivity(this);

        // Auto-connect to signaling server when activity starts
        Log.i("RobotWebRTCActivity", "Auto-connecting to signaling: " + SIGNALING_URL);
        signaling.connect();

        // Xử lý nút Start
        btnStart.setOnClickListener(v -> startWebRTCStream());

        // Xử lý nút Stop
        btnStop.setOnClickListener(v -> stopWebRTCStream());
    }

    private void initializeWebRTC() {
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

        signaling = SignalingSocketManager.getInstance(SIGNALING_URL);
        signaling.setListener(new SignalingSocketManager.Listener() {
            @Override
            public void onConnected() {
                Log.i("RobotWebRTCActivity", "Connected to signaling server");
                // Notify server that robot is ready for WebRTC
                JSONObject readyMsg = new JSONObject();
                try {
                    readyMsg.put("type", "robot_ready");
                    readyMsg.put("robotSerial", SystemHandler.get().getSerialNumber());
                } catch (Exception e) { e.printStackTrace(); }
                signaling.send(readyMsg.toString());

                // Auto-start streaming after connecting to signaling
                Log.i("RobotWebRTCActivity", "Auto-starting WebRTC stream");
                startWebRTCStreamInternal();
            }
            @Override
            public void onMessage(String text) {
                Log.i("RobotWebRTCActivity", "Received signaling message: " + text);
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
            @Override
            public void onDisconnected() {
                Log.i("RobotWebRTCActivity", "Disconnected from signaling server");
            }
            @Override
            public void onError(String error) {
                Log.e("RobotWebRTCActivity", "Signaling error: " + error);
            }
        });
    }

    public void startWebRTCStream() {
        rtcManager.startLocalMedia(this);
        rtcManager.createPeerConnection();
        signaling.connect();
        rtcManager.createOffer();
    }

    public void stopWebRTCStream() {
        if (rtcManager != null) rtcManager.release();
        signaling.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister this activity from the WebRTCHandler
        WebRTCHandler.getInstance().setCurrentActivity(null);
        if (rtcManager != null) rtcManager.release();
        SignalingSocketManager signaling = SignalingSocketManager.getInstance(webSocketUrl + "/signaling/" + SystemHandler.get().getSerialNumber() + "/robot");
        signaling.disconnect();
    }

    private void setupSurfaceView() {
        if (localView != null) {
            // Set scaling type to maintain aspect ratio
            localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            // Enable hardware scaler for better performance
            localView.setEnableHardwareScaler(true);
            // Initially set no mirroring (will be adjusted based on camera)
            localView.setMirror(false);

            // Add layout listener to handle orientation changes
            localView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                // Force redraw when layout changes (orientation/size change)
                localView.requestLayout();
            });
        }
    }

    private void startWebRTCStreamInternal() {
        rtcManager.startLocalMedia(this);
        rtcManager.createPeerConnection();
        rtcManager.createOffer();
    }
}
