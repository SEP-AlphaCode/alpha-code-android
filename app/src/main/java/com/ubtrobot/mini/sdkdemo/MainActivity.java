package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.activity.RobotWebRTCActivity;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.socket.AutoStartManager;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketClient;
import com.ubtrobot.mini.sdkdemo.speech.DemoSpeechJava;
import com.ubtrobot.mini.sdkdemo.uiActivities.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.FaceApiActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.FaceApiCRUD;
import com.ubtrobot.mini.sdkdemo.uiActivities.MicrophoneActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.SpeechApiActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.SysEventTestActivity;
import com.ubtrobot.mini.sdkdemo.uiActivities.TakePicApiActivity;
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
        TTSHandler.init(appContext);
        AutoStartManager.startWebSocketService(appContext);

        // Test log to verify remote logging is working
        LogManager.log(LogLevel.INFO, "MainActivity", "App started successfully", "app_lifecycle", "app_start");

        checkWriteSettingsPermission(this);
        Button forceConnect = (Button) findViewById(R.id.force_connect);
        Button forceWake = (Button) findViewById(R.id.force_wakeup);
        forceConnect.setOnClickListener(l -> {
            RobotSocketClient.getInstance().forceConnect();
        });
        forceWake.setOnClickListener(v -> {
            DemoSpeechJava.getInstance().wakeUp();
            SysApi.get().startup(new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void unused) {

                }

                @Override
                public void onFailure(int i, @NonNull String s) {

                }
            });
        });
    }

    public void micTest(View view){
        Intent intent = new Intent();
        intent.setClass(this, MicrophoneActivity.class);
        startActivity(intent);

    }

    public void robotWebRTCActivity(View view){
        Intent intent = new Intent();
        intent.setClass(this, RobotWebRTCActivity.class);
        startActivity(intent);
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

    public void takePicApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TakePicApiActivity.class);
        startActivity(intent);
    }
    public void faceApiTest(View view){
        Intent intent = new Intent(this, FaceApiActivity.class);
        startActivity(intent);
    }
    public void faceApiCRUD(View view){
        Intent intent = new Intent(this, FaceApiCRUD.class);
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
