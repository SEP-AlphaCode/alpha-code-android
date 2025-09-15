package com.ubtrobot.mini.sdkdemo.log;

import android.util.Log;

import com.ubtrobot.sys.SysApi;

public class LogManager {
    private static final String TAG = "RobotLog";

    private static String robotId = "unknown-robot";
    private static SysApi sysApi;
    private static RemoteLogHandler remoteHandler;
    private static boolean enableRemoteLogging = true;

    public static void init() {
        // Khởi tạo SysApi và lấy robot ID
        sysApi = SysApi.get();
        try {
            if (sysApi != null) {
                String systemName = sysApi.readRobotSid();
                if (systemName != null && !systemName.isEmpty()) {
                    robotId = systemName;
                    Log.i(TAG, "Robot ID from SysApi: " + robotId);
                } else {
                    Log.w(TAG, "Cannot get system name from SysApi, using default");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting system name from SysApi: " + e.getMessage());
        }

        initRemoteHandler();
    }

    // Overloaded method cho manual robot ID nếu cần
    public static void init(String robot) {
        robotId = robot;
        sysApi = SysApi.get();
        initRemoteHandler();
    }

    private static void initRemoteHandler() {
        remoteHandler = new RemoteLogHandler(new RemoteLogHandler.LogCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Remote log sent successfully");
            }

            @Override
            public void onFailure(String error) {
                Log.w(TAG, "Remote log failed: " + error);
            }
        });
    }

    // Method mới để log với LogLevel enum
    public static void log(LogLevel level,String tag, String message) {
        logMessage(level, tag, message);
    }

    private static void logMessage(LogLevel level, String tag, String message) {
        // Log to Android logcat with appropriate level
        switch (level) {
            case INFO:
                Log.i(TAG, "[" + level + "] " + message);
                break;
            case ERROR:
                Log.e(TAG, "[" + level + "] " + message);
                break;
            case WARN:
                Log.w(TAG, "[" + level + "] " + message);
                break;
            case DEBUG:
                Log.d(TAG, "[" + level + "] " + message);
                break;
            default:
                Log.i(TAG, "[" + level + "] " + message);
                break;
        }

        // Send to remote server if enabled
        if (enableRemoteLogging && remoteHandler != null) {
            LogEntry logEntry = new LogEntry(robotId, level.getValue(), tag, message, System.currentTimeMillis());
            handleRemoteLogging(logEntry);
        }
    }

    private static void handleRemoteLogging(LogEntry logEntry) {
        try {
            remoteHandler.sendLog(logEntry);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send log remotely", e);
        }
    }
}