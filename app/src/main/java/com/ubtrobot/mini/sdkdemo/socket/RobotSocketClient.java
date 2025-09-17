package com.ubtrobot.mini.sdkdemo.socket;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.ubtrobot.mini.sdkdemo.BuildConfig;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;

public class RobotSocketClient extends Service {
    private RobotSocketController robotController;
    private RobotSocketManager manager;
    private void init(){
        Log.i("RobotSocketClient", "Connecting...");
        robotController = new RobotSocketController();
        String path = BuildConfig.API_WEBSOCKET;
        manager = new RobotSocketManager(path, robotController);
        SystemHandler.get().setSocketManager(manager);
    }

    /**
     * Used for registering the service
     */
    public RobotSocketClient(){
    }
    public RobotSocketClient(RobotSocketController robotController) {
        this.robotController = robotController;
        init();
    }

    public void forceConnect(){
        manager.connect();
        Log.i("WebSocketManager", "Done");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
