package com.ubtrobot.mini.sdkdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.webrtc.*;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class RobotWebRTCActivity extends AppCompatActivity {
    private WebRTCManager rtcManager;
    private SignalingClient signalingClient;
    private static final String SIGNALING_URL = BuildConfig.API_WEBSOCKET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_webrtc);

        SurfaceViewRenderer localView = findViewById(R.id.localView);

        rtcManager = new WebRTCManager(this, localView, new WebRTCManager.Callback() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "ice");
                    JSONObject cand = new JSONObject();
                    cand.put("sdpMid", candidate.sdpMid);
                    cand.put("sdpMLineIndex", candidate.sdpMLineIndex);
                    cand.put("candidate", candidate.sdp);
                    msg.put("candidate", cand);
                    signalingClient.send(msg);
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onLocalDescription(SessionDescription sdp) {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("type", sdp.type.canonicalForm());
                    msg.put("sdp", sdp.description);
                    signalingClient.send(msg);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        rtcManager.startLocalMedia(this);
        rtcManager.createPeerConnection();

        signalingClient = new SignalingClient(SIGNALING_URL, new SignalingClient.Listener() {
            @Override
            public void onOfferReceived(JSONObject offer) {
                // Nếu robot đóng vai answerer thì xử lý ở đây
            }

            @Override
            public void onAnswerReceived(JSONObject answer) {
                try {
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.ANSWER,
                            answer.getString("sdp"));
                    rtcManager.setRemoteDescription(sdp);
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onIceCandidateReceived(JSONObject candidate) {
                try {
                    JSONObject c = candidate.getJSONObject("candidate");
                    rtcManager.addIceCandidate(new IceCandidate(
                            c.getString("sdpMid"),
                            c.getInt("sdpMLineIndex"),
                            c.getString("candidate")));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // Tạo offer ngay khi khởi động
        rtcManager.createOffer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtcManager != null) rtcManager.release();
    }
}
