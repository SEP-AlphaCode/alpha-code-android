package com.ubtrobot.mini.sdkdemo.log;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import okhttp3.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;

public class RemoteLogHandler {
    private static final String TAG = "RemoteLogHandler";

    private LogService logService;
    private LogCallback logCallback;

    // Interface cho Retrofit
    public interface LogService {
        @POST("logs")
        Call<Void> sendLog(@Body LogEntry logEntry);
    }

    public interface LogCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public RemoteLogHandler() {
        initLogService();
    }

    public RemoteLogHandler(LogCallback callback) {
        this.logCallback = callback;
        initLogService();
    }

    private void initLogService() {
        try {
            Retrofit retrofit = ApiClient.getLogServiceInstance();
            logService = retrofit.create(LogService.class);
            Log.d(TAG, "LogService initialized successfully using ApiClient");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize LogService", e);
        }
    }

    public void sendLog(LogEntry logEntry) {
        sendLogAsync(logEntry, logCallback);
    }

    public void sendLogAsync(LogEntry logEntry, LogCallback callback) {
        try {
            if (logService == null) {
                String errorMsg = "LogService is not initialized";
                Log.e(TAG, errorMsg);
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
                return;
            }

            Call<Void> call = logService.sendLog(logEntry);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        String errorMsg = "Server returned error: " + response.code() + " " + response.message();
                        Log.e(TAG, errorMsg);

                        // Try to read response body for more details
                        try {
                            String responseBody = response.errorBody() != null ? response.errorBody().string() : "No response body";
                            Log.e(TAG, "Error response body: " + responseBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Could not read error response body", e);
                        }

                        if (callback != null) {
                            callback.onFailure(errorMsg);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    String errorMsg = "Failed to send log to server: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    if (callback != null) {
                        callback.onFailure(errorMsg);
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
        if (logService == null) {
            throw new IOException("LogService is not initialized");
        }

        try {
            Response<Void> response = logService.sendLog(logEntry).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Server returned error: " + response.code() + " " + response.message());
            }
            Log.d(TAG, "Log sent successfully to server (sync)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to send log synchronously", e);
            throw new IOException("Failed to send log: " + e.getMessage(), e);
        }
    }
}
