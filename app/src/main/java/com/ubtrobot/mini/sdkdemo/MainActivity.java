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
    private static final String TAG = "MainActivity";
    private RobotSocketClient wsClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Button forceConnect = (Button) findViewById(R.id.force_connect);
        DanceWithMusicActivity danceActivity = DanceWithMusicActivity.get();
        TakePicApiActivity takePicApiActivity = TakePicApiActivity.get();
        ActionApiActivity actionApiActivity = ActionApiActivity.get();
        RobotSocketController robotSocketController = new RobotSocketController(danceActivity, takePicApiActivity, actionApiActivity);
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

    public void takePicApiTest(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TakePicApiActivity.class);
        startActivity(intent);
    }
}
