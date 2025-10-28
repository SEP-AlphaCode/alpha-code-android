package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.socket.AutoStartManager;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketClient;
import com.ubtrobot.mini.sdkdemo.speech.DemoSpeechJava;
import com.ubtrobot.mini.sdkdemo.uiActivities.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.MicrophoneActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.SpeechApiActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.SysEventTestActivity;
import com.ubtrobot.sys.SysApi;

/**
 * Created by lulin.wu on 2018/6/19.
 */

public class MainActivity extends Activity {
    public static final String TAG = "DEBUG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Context appContext = Utils.getContext().getApplicationContext();
        AutoStartManager.startWebSocketService(appContext);
        checkWriteSettingsPermission(this);
        Button forceConnect = (Button) findViewById(R.id.force_connect);
        Button forceWake = (Button) findViewById(R.id.force_wakeup);
        forceConnect.setOnClickListener(l -> {
            RobotSocketClient.getInstance().forceConnect();
        });
        forceWake.setOnClickListener(v -> {
            DemoSpeechJava.getInstance().wakeUp();
        });
    }

    public void micTest(View view){
        Intent intent = new Intent();
        intent.setClass(this, MicrophoneActivity.class);
        startActivity(intent);
        SysApi.get().shutdown();
    }
    public void speechApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SpeechApiActivity.class);
        startActivity(intent);
    }

    public void actionApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ActionApiActivity.class);
        startActivity(intent);
    }

    public void sysEventTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SysEventTestActivity.class);
        startActivity(intent);
    }
    private void checkWriteSettingsPermission(Context context) {
        if (!Settings.System.canWrite(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }
}
