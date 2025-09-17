package com.ubtrobot.mini.sdkdemo.custom;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.apis.WebsocketApi;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.sys.SysApi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShutdownReceiver extends BroadcastReceiver {
    private static String TAG = "ShutdownReceiver";
    private WebsocketApi ws = ApiClient.getPythonInstance().create(WebsocketApi.class);
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            // Thực hiện hành động của bạn ở đây
            performShutdownAction(context);
        }
    }

    private void performShutdownAction(Context context) {
        // Ví dụ: gửi thông báo, lưu trạng thái, đồng bộ dữ liệu cuối cùng
        Log.d("ShutdownReceiver", "Thiết bị đang tắt nguồn");
        // Lưu trạng thái ứng dụng
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putLong("last_shutdown_time", System.currentTimeMillis()).apply();
        String serial = SysApi.get().readRobotSid();
        ws.disconnect(serial).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        LogManager.log(LogLevel.INFO, TAG, "Robot " + serial + " is shutting down");
    }
}
