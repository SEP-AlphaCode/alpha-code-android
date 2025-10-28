package com.ubtrobot.mini.sdkdemo.webrtc;

import android.util.Log;
import org.json.JSONObject;
import okhttp3.*;

public class SignalingClient extends WebSocketListener {
    private static final String TAG = "SignalingClient";

    public interface Listener {
        void onOfferReceived(JSONObject offer);
        void onAnswerReceived(JSONObject answer);
        void onIceCandidateReceived(JSONObject candidate);
    }

    private WebSocket ws;
    private Listener listener;

    public SignalingClient(String url, Listener listener) {
        this.listener = listener;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        ws = client.newWebSocket(request, this);
    }

    public void send(JSONObject obj) {
        if (ws != null) ws.send(obj.toString());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JSONObject msg = new JSONObject(text);
            String type = msg.getString("type");
            switch (type) {
                case "offer": listener.onOfferReceived(msg); break;
                case "answer": listener.onAnswerReceived(msg); break;
                case "ice": listener.onIceCandidateReceived(msg); break;
            }
        } catch (Exception e) { Log.e(TAG, "parse error", e); }
    }
}
