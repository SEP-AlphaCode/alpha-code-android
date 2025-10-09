// RobotSocketClient.java
package com.ubtrobot.mini.sdkdemo.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.mini.sdkdemo.BuildConfig;

public class RobotSocketClient extends Service {
    private static final String TAG = "RobotSocketClient";
    private static final int DELAY_AFTER_BOOT_MS = 10000; // 10 seconds delay after boot

    private RobotSocketManager socketManager;
    private RobotSocketController socketController;
    private static RobotSocketClient instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service creating...");
        instance = this;

        // Delay initialization to ensure system is fully booted
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(this::initialize, DELAY_AFTER_BOOT_MS);
    }

    private void initialize() {
        try {
            Log.i(TAG, "Initializing WebSocket service...");

            // Get robot serial
            String serial = SysApi.get().readRobotSid();
            if (serial == null || serial.isEmpty()) {
                serial = "unknown_serial";
            }

            // Create controller and manager
            socketController = new RobotSocketController();
            socketManager = RobotSocketManager.getInstance(BuildConfig.API_WEBSOCKET, serial);

            // Connect them
            socketManager.setView(socketController);

            // Register with system
            SystemHandler.get().setSocketManager(socketManager);

            // Start connection
            socketManager.connect();

            Log.i(TAG, "Service initialized successfully and connection started");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service", e);
            // Schedule retry if initialization fails
            scheduleRetry();
        }
    }

    private void scheduleRetry() {
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            Log.i(TAG, "Retrying initialization...");
            initialize();
        }, 30000); // Retry after 30 seconds
    }

    public void forceConnect() {
        if (socketManager != null) {
            Log.d(TAG, "Forcing connection");
            socketManager.connect();
        } else {
            Log.w(TAG, "SocketManager not available, reinitializing...");
            initialize();
        }
    }

    public static RobotSocketClient getInstance() {
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service starting with flags: " + flags);

        // If service was killed and restarted, reinitialize
        if (socketManager == null) {
            Log.i(TAG, "Service restarted, reinitializing...");
            initialize();
        } else if (!socketManager.isConnected()) {
            Log.i(TAG, "Service started but not connected, reconnecting...");
            socketManager.connect();
        }

        return START_STICKY; // Important: Service will be restarted if killed
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroying");
        if (socketManager != null) {
            socketManager.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Helper method to check if service is running and connected
    public static boolean isServiceRunningAndConnected() {
        return instance != null &&
                instance.socketManager != null &&
                instance.socketManager.isConnected();
    }
}