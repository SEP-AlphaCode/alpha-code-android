package com.ubtrobot.mini.sdkdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class RobotWebSocketService extends Service {
    public static final String TAG = "RobotWebSocketService";
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Created");
        startWebSocketClient();
    }

    private void startWebSocketClient() {
        URI serverUri = URI.create("ws://192.168.1.177:8000/websocket/ws"); // Replace with your server details

        webSocketClient = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d(TAG, "WebSocket connection opened");
                isConnected = true;
                sendBroadcast(new Intent("WEBSOCKET_CONNECTION").putExtra("status", "connected"));
                VoicePool vp = VoicePool.get();
                vp.playTTs("I am connected to the server", Priority.HIGH, null);
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Received message: " + message);
                // Process the received command
                foo(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "WebSocket connection closed: " + reason);
                isConnected = false;
                sendBroadcast(new Intent("WEBSOCKET_CONNECTION").putExtra("status", "disconnected"));
                // Attempt to reconnect
                reconnect();
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "WebSocket error: " + ex.getMessage());
                isConnected = false;
                sendBroadcast(new Intent("WEBSOCKET_CONNECTION").putExtra("status", "error"));
            }
        };

        webSocketClient.connect();
    }

    private void reconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds before reconnecting
                if (webSocketClient != null) {
                    webSocketClient.reconnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void foo(String command) {
        // Implement your robot's command execution logic here
        Log.d(TAG, "Executing command: " + command);
        VoicePool vp = VoicePool.get();
        vp.playTTs(command, Priority.HIGH, new VoiceListener() {
            @Override
            public void onCompleted() {
                Log.e(TAG, "Done");
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "Error: " + s);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}