package com.ubtrobot.mini.sdkdemo.socket;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.lib.mouthledapi.MouthLedApi;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.utils.LedHelper;
import com.ubtrobot.sys.SysApi;

public class RobotSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final long RECONNECT_DELAY_MS = 7500;
    private static final long PING_INTERVAL_MS = 15000; // 30 seconds ping interval
    private OkHttpClient client;
    private WebSocket webSocket;
    private Request request;
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private RobotSocketController robotController;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable connectionChecker;
    private SysApi sysApi;
    private LedHelper ledHelper;

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            builder.readTimeout(0, TimeUnit.MILLISECONDS);
            builder.pingInterval(PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
            builder.retryOnConnectionFailure(true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RobotSocketManager(String serverUrl, RobotSocketController robotController) {
        this.robotController = robotController;
        ledHelper = new LedHelper();
        sysApi = SysApi.get();

        // Use unsafe client to allow self-signed certificates
        client = getUnsafeOkHttpClient();

        String serial = getRobotSerialNumber();
        request = new Request.Builder()
                .url(serverUrl + "/" + serial)
                .build();

//        setupConnectionChecker();
//        connect();
    }

    private String getRobotSerialNumber() {
        String serialNumber = sysApi.readRobotSid();;
        if (serialNumber == null || serialNumber.isEmpty()) {
            serialNumber = "unknown_serial";
        }
        return serialNumber;
    }

    private void setupConnectionChecker() {
        connectionChecker = new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    ledHelper.notifyState(1);
                    connect();
                }
                handler.postDelayed(this, PING_INTERVAL_MS); // Check every ping interval
            }
        };
    }

    public void connect() {
        if (isConnected) return;

        handler.removeCallbacks(connectionChecker);
        handler.post(connectionChecker);

        webSocket = client.newWebSocket(request,  new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                //ttsHandler.doTTS("Connected", "en");
                Log.d(TAG, "WebSocket connected");
                ledHelper.notifyState(0);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                ledHelper.notifyState(0);
                if (robotController != null) {
                    Log.i(TAG, "Received message: " + text);
                    robotController.handleCommand(text, "en");
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                handleConnectionFailure(t.getMessage());
                //ttsHandler.doTTS("Failed", "en");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                handleConnectionFailure("Connection closed: " + reason);
                //ttsHandler.doTTS("Closed", "en");
            }
        });
    }

    private void handleConnectionFailure(String error) {
        isConnected = false;
        Log.e(TAG, "WebSocket error: " + error);
        ledHelper.notifyState(2);

        if (shouldReconnect) {
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        try {
            Log.i(TAG, "Scheduling reconnect in " + RECONNECT_DELAY_MS + "ms");
            handler.removeCallbacksAndMessages(null); // Clear any pending reconnects
            handler.postDelayed(() -> {
                //notifyState(1);
                if (!isConnected && shouldReconnect) {
                    Log.i(TAG, "Attempting to reconnect");
                    //vp.playTTs("Attempting to reconnect", Priority.HIGH, null);
                    connect();
                }
            }, RECONNECT_DELAY_MS);
        } catch (Exception e) {
            Log.e(TAG, "Reconnect scheduling error: " + e.toString());
        }
    }

    public void sendMessage(String message) {
        if (webSocket != null && isConnected) {
            boolean success = webSocket.send(message);
            Log.d(TAG, "Send message: " + message + " | success: " + success);
        } else {
            Log.w(TAG, "Cannot send message, WebSocket not connected.");
        }
    }

    public void disconnect() {
        shouldReconnect = false;
        handler.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            webSocket.close(1000, "Disconnected by user");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}