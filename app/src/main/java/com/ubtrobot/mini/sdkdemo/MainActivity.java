package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketClient;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketController;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;

import java.util.Arrays;

/**
 * Created by lulin.wu on 2018/6/19.
 */

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private RobotSocketClient wsClient;
    private VP vp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vp = VP.get();
        setContentView(R.layout.main_layout);
//        Button forceConnect = (Button) findViewById(R.id.force_connect);
//        DanceWithMusicActivity danceActivity = DanceWithMusicActivity.get();
//        TakePicApiActivity takePicApiActivity = TakePicApiActivity.get();
//        ActionApiActivity actionApiActivity = ActionApiActivity.get();
//        RobotSocketController robotSocketController = new RobotSocketController(danceActivity, takePicApiActivity, actionApiActivity);
//        wsClient = new RobotSocketClient(robotSocketController);
//        forceConnect.setOnClickListener(l -> {
//            try{
//                wsClient.forceConnect();
//            } catch (Exception e) {
//                Log.e("WebSocketManager", e.toString());
//            }
//        });
        vp.playTTs("Text to speech", Priority.HIGH, new VP.Listener() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Request req, CallException var2) {
                Log.e(TAG, "Error: " + var2.getCode() + ", " + var2.getSubCode() + ", " + var2.getParam());
                Log.e(TAG, "Error string: " + var2);
                Log.e(TAG, "Error msg: " + var2.getMessage() + ", localised: " + var2.getLocalizedMessage());
                Log.e(TAG, "Req id: " + req.getId());
                Log.e(TAG, "Req path: " + req.getPath());
                Log.e(TAG, "Req time: " + req.getWhen());
                Log.e(TAG, "Req param: " + req.getParam());
                Log.e(TAG, "Cause: " + var2.getCause());
                Log.e(TAG, Log.getStackTraceString(var2));
                Log.e(TAG, Arrays.toString(var2.getSuppressed()));
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
