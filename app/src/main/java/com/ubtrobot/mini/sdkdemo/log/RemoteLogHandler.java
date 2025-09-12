package com.ubtrobot.mini.sdkdemo.log;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.BuildConfig;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RemoteLogHandler {
    private static final String TAG = "RemoteLogHandler";
    private static final String BASE_URL = BuildConfig.API_LOGSERVICE_PATH;
    private static final int CONNECT_TIMEOUT = 10; // seconds
    private static final int READ_TIMEOUT = 30; // seconds
    private static final int WRITE_TIMEOUT = 30; // seconds

    private OkHttpClient client;
    private LogCallback logCallback;

    public interface LogCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public RemoteLogHandler() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public RemoteLogHandler(LogCallback callback) {
        this();
        this.logCallback = callback;
    }

    public void sendLog(LogEntry logEntry) {
        sendLogAsync(logEntry, logCallback);
    }

    public void sendLogAsync(LogEntry logEntry, LogCallback callback) {
        try {
            String json = logEntry.toJson();

            RequestBody body = RequestBody.create(
                    json,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "RobotLogClient/1.0")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String errorMsg = "Failed to send log to server: " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    if (callback != null) {
                        callback.onFailure(errorMsg);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Log sent successfully to server");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else {
                            String errorMsg = "Server returned error: " + response.code() + " " + response.message();
                            Log.e(TAG, errorMsg);
                            if (callback != null) {
                                callback.onFailure(errorMsg);
                            }
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            String errorMsg = "Error preparing log request: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            if (callback != null) {
                callback.onFailure(errorMsg);
            }
        }
    }

    public void sendLogSync(LogEntry logEntry) throws IOException {
        String json = logEntry.toJson();

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "RobotLogClient/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Server returned error: " + response.code() + " " + response.message());
            }
            Log.d(TAG, "Log sent successfully to server (sync)");
        }
    }

    public void setBaseUrl(String baseUrl) {
        // Note: This would require recreating the client or using interceptors
        // For simplicity, we'll log the change request
        Log.i(TAG, "Base URL change requested: " + baseUrl);
    }

    public void shutdown() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}
