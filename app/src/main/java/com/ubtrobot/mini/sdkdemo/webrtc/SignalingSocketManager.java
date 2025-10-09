package com.ubtrobot.mini.sdkdemo.webrtc;

import android.os.Looper;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import android.os.Handler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SignalingSocketManager {
    private static SignalingSocketManager instance;
    private OkHttpClient client;
    private WebSocket ws;
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable;
    private String url;
    private Listener listener;

    public interface Listener {
        void onConnected();
        void onMessage(String text);
        void onDisconnected();
        void onError(String error);
    }

    private static final long PING_INTERVAL_MS = 15000;

    private SignalingSocketManager(String url) {
        this.url = url;
        client = createUnsafeOkHttpClient();
    }

    public static synchronized SignalingSocketManager getInstance(String url) {
        if (instance == null) instance = new SignalingSocketManager(url);
        return instance;
    }

    public void setListener(Listener listener) { this.listener = listener; }

    private OkHttpClient createUnsafeOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .pingInterval(PING_INTERVAL_MS, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create OkHttpClient", e);
        }
    }

    public void connect() {
        if (isConnected) return;
        Request req = new Request.Builder().url(url).build();
        ws = client.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                isConnected = true;
                if (listener != null) listener.onConnected();
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (listener != null) listener.onMessage(text);
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                isConnected = false;
                if (listener != null) listener.onError(t.getMessage());
                scheduleReconnect();
            }
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                if (listener != null) listener.onDisconnected();
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if (!shouldReconnect) return;
        if (reconnectRunnable != null) handler.removeCallbacks(reconnectRunnable);
        reconnectRunnable = () -> { if (!isConnected) connect(); };
        handler.postDelayed(reconnectRunnable, 5000);
    }

    public void send(String msg) {
        if (ws != null && isConnected) ws.send(msg);
    }

    public void disconnect() {
        shouldReconnect = false;
        if (ws != null) ws.close(1000, "Disconnected");
    }
}

