package com.ubtrobot.mini.sdkdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.socket.RobotSocketClient;

// Create a BroadcastReceiver to start service on boot
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(new Intent(context, RobotSocketClient.class));
            Log.i("ACTION_BOOT", "Init service");
        }
    }
}