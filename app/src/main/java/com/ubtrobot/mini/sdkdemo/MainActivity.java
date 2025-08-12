package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ubtrobot.mini.sdkdemo.socket.RobotSocketClient;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketController;

/**
 * Created by lulin.wu on 2018/6/19.
 */

public class MainActivity extends Activity {
    private static final String TAG = DemoApp.DEBUG_TAG;
    private RobotSocketClient wsClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Button forceConnect = (Button) findViewById(R.id.force_connect);
        DanceWithMusicActivity danceActivity = DanceWithMusicActivity.get();
        RobotSocketController robotSocketController = new RobotSocketController(danceActivity);
        wsClient = new RobotSocketClient(robotSocketController);
        forceConnect.setOnClickListener(l -> {
            try{
                wsClient.forceConnect();
            } catch (Exception e) {
                Log.e("WebSocketManager", e.toString());
            }
        });
    }

    public void actionApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ActionApiActivity.class);
        startActivity(intent);
    }

    public void musicApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, DanceWithMusicActivity.class);
        startActivity(intent);
    }

    public void expressApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ExpressApiActivity.class);
        startActivity(intent);
    }

    public void motorApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, MotorApiActivity.class);
        startActivity(intent);
    }

    public void faceApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, FaceApiActivity.class);
        startActivity(intent);
    }

    public void objectDetectApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ObjectDetectApiActivity.class);
        startActivity(intent);
    }

    public void takePicApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TakePicApiActivity.class);
        startActivity(intent);
    }

    public void mouthLedApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, MouthLedApiActivity.class);
        startActivity(intent);
    }

    public void sysEventApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SysEventApiActivity.class);
        startActivity(intent);
    }

    public void sysApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SysApiActivity.class);
        startActivity(intent);
    }

    public void standupApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, StandupApiActivity.class);
        startActivity(intent);
    }

    public void OnRobotInit(View view){
        Intent intent = new Intent(this,RobotInitActivity.class);
        startActivity(intent);
    }

    public void skillApiTest(View view) {
        startActivity(new Intent(this, SkillApiActivity.class));
    }

    public void powerApiTest(View view) {
        startActivity(new Intent(this, PowerApiActivity.class));
    }


    public void onStartLanTest(View view){
        startActivity(new Intent(this,DiscorverActivity.class));
    }

    public void phoneCallTest(View view) {
        startActivity(new Intent(this, PhoneCallApiActivity.class));
    }

    public void speechApiTest(View view) {
        startActivity(new Intent(this, SpeechApiActivity.class));
    }

    public void ApkInstallTest(View view) {
        startActivity(new Intent(this, ApkSilentInstallerApiActivity.class));
    }

    public void BuiltInSkillTest(View view) {
        startActivity(new Intent(this, BuiltInSkillCallTestActivity.class));
    }
}
