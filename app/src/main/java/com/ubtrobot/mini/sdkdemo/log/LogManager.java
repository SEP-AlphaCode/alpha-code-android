package com.ubtrobot.mini.sdkdemo.log;

import android.util.Log;

import com.ubtrobot.sys.SysApi;

public class LogManager {
    private static final String TAG = "RobotLog";

    private static String robotId = "unknown-robot";
    private static SysApi sysApi;
    private static RemoteLogHandler remoteHandler;
    private static boolean enableRemoteLogging = true;

    // Lưu tạm accountLessonId khi đang trong submission
    private static String currentAccountLessonId = null;


    public static void init() {
        // Khởi tạo SysApi và lấy robot ID
        sysApi = SysApi.get();
        try {
            if (sysApi != null) {
                String systemName = sysApi.readRobotSid();
                if (systemName != null && !systemName.isEmpty()) {
                    robotId = systemName;
                } else {
                    Log.w(TAG, "Cannot get system name from SysApi, using default");
                }
            } else {
                Log.w(TAG, "SysApi is null, using default robot ID");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting system name from SysApi: " + e.getMessage(), e);
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
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RemoteLogHandler", e);
            remoteHandler = null;
        }
    }

    // Method cơ bản
    public static void log(LogLevel level, String tag, String message) {
        logMessage(level, tag, message, null, null);
    }

    // Method với type và code
    public static void log(LogLevel level, String tag, String message, String type, String code) {
        logMessage(level, tag, message, type, code);
    }

    public static String getRobotId() {
        return robotId;
    }

    // Bắt đầu submission - lưu accountLessonId
    public static void startSubmission(String accountLessonId) {
        currentAccountLessonId = accountLessonId;
        Log.i(TAG, "Submission started for accountLessonId: " + accountLessonId);
        log(LogLevel.INFO, "submission_start", "Submission started for " + accountLessonId, "submission_start", null);
    }

    // Kết thúc submission - xóa accountLessonId
    public static void endSubmission() {
        if (currentAccountLessonId != null) {
            Log.i(TAG, "Submission ended for accountLessonId: " + currentAccountLessonId);
            log(LogLevel.INFO, "submission_end", "Submission ended for " + currentAccountLessonId, "submission_end", null);
            currentAccountLessonId = null;  // Xóa
        } else {
            Log.w(TAG, "No active submission to end");
        }
    }

    public static String getCurrentAccountLessonId() {
        return currentAccountLessonId;
    }

    public static boolean isSubmissionActive() {
        return currentAccountLessonId != null;
    }


    private static void logMessage(LogLevel level, String tag, String message, String type, String code) {
        // Log to Android logcat
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
            try {
                LogEntry logEntry = new LogEntry(
                    robotId,
                    level.getValue(),
                    tag,
                    message,
                    System.currentTimeMillis(),
                    currentAccountLessonId,  // Tự động lấy từ static variable
                    type,
                    code
                );
                handleRemoteLogging(logEntry);
            } catch (Exception e) {
                Log.e(TAG, "Error creating or sending LogEntry", e);
            }
        } else {
            if (!enableRemoteLogging) {
                Log.d(TAG, "Remote logging is disabled");
            }
            if (remoteHandler == null) {
                Log.w(TAG, "RemoteHandler is null - remote logging unavailable");
            }
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