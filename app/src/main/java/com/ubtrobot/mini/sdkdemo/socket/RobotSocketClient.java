// RobotSocketClient.java
package com.ubtrobot.mini.sdkdemo.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.mini.sdkdemo.utils.LedHelper;
import com.ubtrobot.sys.SysApi;

public class RobotSocketClient extends Service {
    private static final String TAG = "RobotSocketClient";

    private RobotSocketManager socketManager;
    private RobotSocketController socketController;
    private static RobotSocketClient instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service creating...");
        initialize();
        instance = this;
    }

    private void initialize() {
        try {
            // Get robot serial
            String serial = SysApi.get().readRobotSid();
            if (serial == null || serial.isEmpty()) {
                serial = "unknown_serial";
            }

            // Create dependencies
            CommandHandler commandHandler = new CommandHandler();
            LedHelper ledHelper = new LedHelper();

            // Create controller and manager
            socketController = new RobotSocketController();
            socketManager = new RobotSocketManager(BuildConfig.API_WEBSOCKET, serial);

            // Connect them
            socketManager.setView(socketController);

            // Register with system
            SystemHandler.get().setSocketManager(socketManager);

            Log.i(TAG, "Service initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize service", e);
        }
    }

    public void forceConnect() {
        if (socketManager != null) {
            Log.d(TAG, "Forcing connection");
            socketManager.connect();
        }
    }

    public static RobotSocketClient getInstance() {
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service starting");
        if (socketManager != null && !socketManager.isConnected()) {
            socketManager.connect();
        }
        return START_STICKY;
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
}