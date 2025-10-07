// RobotSocketManager.java
package com.ubtrobot.mini.sdkdemo.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

public class RobotSocketManager implements WebSocketContract.Presenter {
    private static final String TAG = "RobotSocketManager";
    private static final long RECONNECT_DELAY_MS = 7500;
    private static final long PING_INTERVAL_MS = 15000;

    private OkHttpClient client;
    private WebSocket webSocket;
    private Request request;
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private WebSocketContract.View view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pingRunnable;

    private final String serverUrl;
    private final String robotSerial;
    private static RobotSocketManager instance;

    private RobotSocketManager(String serverUrl, String robotSerial) {
        this.serverUrl = serverUrl;
        this.robotSerial = robotSerial;
        initializeClient();
        setupPingMechanism();
    }

    public static synchronized RobotSocketManager getInstance(String serverUrl, String robotSerial) {
        if (instance == null) {
            instance = new RobotSocketManager(serverUrl, robotSerial);
        }
        return instance;
    }

    public static synchronized RobotSocketManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RobotSocketManager not initialized. Call getInstance(String, String) first.");
        }
        return instance;
    }

    // Method to check if instance is initialized
    public static boolean isInitialized() {
        return instance != null;
    }

    @Override
    public void setView(WebSocketContract.View view) {
        this.view = view;
    }

    private void initializeClient() {
        client = createUnsafeOkHttpClient();
        request = new Request.Builder()
                .url(serverUrl + "/" + robotSerial)
                .build();
    }

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

    private void setupPingMechanism() {
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isConnected && webSocket != null) {
                    // Ping is handled automatically by OkHttp with pingInterval
                    Log.d(TAG, "Ping check - connection active");
                }
                handler.postDelayed(this, PING_INTERVAL_MS);
            }
        };
    }

    @Override
    public void connect() {
        if (isConnected) {
            Log.d(TAG, "Already connected, skipping connection attempt");
            return;
        }

        handler.removeCallbacks(pingRunnable);
        handler.post(pingRunnable);

        try {
            webSocket = client.newWebSocket(request, new SocketListener());
            Log.d(TAG, "Connection attempt initiated");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create WebSocket connection", e);
            handleConnectionFailure("Connection creation failed: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        shouldReconnect = false;
        cleanup();

        if (webSocket != null) {
            webSocket.close(1000, "Disconnected by user");
        }

        if (view != null) {
            view.onDisconnected();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (webSocket != null && isConnected) {
            boolean success = webSocket.send(message);
            Log.d(TAG, "Send message: " + message + " | success: " + success);

            if (!success && view != null) {
                view.onError("Failed to send message");
            }
        } else {
            Log.w(TAG, "Cannot send message, WebSocket not connected");
            if (view != null) {
                view.onError("WebSocket not connected");
            }
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    private void handleConnectionFailure(String error) {
        isConnected = false;
        Log.e(TAG, "WebSocket error: " + error);

        if (view != null) {
            view.onError(error);
        }

        if (shouldReconnect) {
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        try {
            Log.i(TAG, "Scheduling reconnect in " + RECONNECT_DELAY_MS + "ms");
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> {
                if (!isConnected && shouldReconnect) {
                    Log.i(TAG, "Attempting to reconnect");
                    connect();
                }
            }, RECONNECT_DELAY_MS);
        } catch (Exception e) {
            Log.e(TAG, "Reconnect scheduling error", e);
        }
    }

    private void cleanup() {
        handler.removeCallbacksAndMessages(null);
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    @Override
    public void sendBinaryMessage(byte[] message) {
        if (webSocket != null && isConnected) {
            boolean success = webSocket.send(okio.ByteString.of(message));
            Log.d(TAG, "Send binary message, length: " + message.length + " | success: " + success);

            if (!success && view != null) {
                view.onError("Failed to send binary message");
            }
        } else {
            Log.w(TAG, "Cannot send binary message, WebSocket not connected");
        }
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            isConnected = true;
            Log.d(TAG, "WebSocket connected successfully");

            if (view != null) {
                view.onConnected();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.i(TAG, "Received text message: " + text);

            if (view != null) {
                view.onMessageReceived(text);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            handleConnectionFailure("Connection failed: " + t.getMessage());
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            handleConnectionFailure("Connection closed: " + reason + " (code: " + code + ")");
        }
        @Override
        public void onMessage(WebSocket webSocket, okio.ByteString bytes) {
            Log.i(TAG, "Received binary message, length: " + bytes.size());

            // Handle binary response (if your server sends protobuf responses)
            if (view != null) {
                // Convert to string for text responses, or handle as binary
                String textResponse = bytes.utf8();
                view.onMessageReceived(textResponse);
            }
        }
    }
}