package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.activity.RobotWebRTCActivity;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;

public class WebRTCHandler {
    private static final String TAG = "WebRTCHandler";
    private RobotSocketManager socketManager;
    private static WebRTCHandler instance;
    private RobotWebRTCActivity currentActivity;

    public static synchronized WebRTCHandler getInstance() {
        if (instance == null) {
            instance = new WebRTCHandler();
        }
        return instance;
    }

    private WebRTCHandler() {
        // Private constructor for singleton
    }

    public void setSocketManager(RobotSocketManager socketManager) {
        this.socketManager = socketManager;
    }

    public void setCurrentActivity(RobotWebRTCActivity activity) {
        this.currentActivity = activity;
    }

    public void handleWebRTCStart(Context context) {
        Log.i(TAG, "Starting WebRTC");

        if (currentActivity != null) {
            // If activity is already running, just start the stream
            currentActivity.startWebRTCStream();
        } else {
            // Start the WebRTC activity
            Intent intent = new Intent(context, RobotWebRTCActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // Send response back through socket
        if (socketManager != null) {
            socketManager.sendMessage("{\"type\":\"webrtc_start_response\",\"status\":\"success\",\"message\":\"WebRTC started\"}");
        }
    }

    public void handleWebRTCStop() {
        Log.i(TAG, "Stopping WebRTC");

        if (currentActivity != null) {
            currentActivity.stopWebRTCStream();
        }

        // Send response back through socket
        if (socketManager != null) {
            socketManager.sendMessage("{\"type\":\"webrtc_stop_response\",\"status\":\"success\",\"message\":\"WebRTC stopped\"}");
        }
    }
}
