package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    public static final String TAG = "DEBUG";
    private RobotSocketClient wsClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        checkWriteSettingsPermission(this);
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
    private void checkWriteSettingsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }
    }
}
