package com.ubtrobot.mini.sdkdemo.socket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
public class AutoStartManager {
    private static final String TAG = "AutoStartManager";

    /**
     * Start WebSocket service manually (can be called from your main activity)
     */
    public static void startWebSocketService(Context context) {
        Log.i(TAG, "Manually starting WebSocket service");
        Intent serviceIntent = new Intent(context, RobotSocketClient.class);
        context.startService(serviceIntent);
    }

    /**
     * Stop WebSocket service manually
     */
    public static void stopWebSocketService(Context context) {
        Log.i(TAG, "Stopping WebSocket service");
        Intent serviceIntent = new Intent(context, RobotSocketClient.class);
        context.stopService(serviceIntent);
    }

    /**
     * Check if service should auto-start
     */
    public static boolean shouldAutoStart(Context context) {
        return true; // Default to auto-start
    }
}