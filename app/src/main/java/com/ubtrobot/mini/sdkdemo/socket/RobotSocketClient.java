package com.ubtrobot.mini.sdkdemo.socket;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;
import com.ubtrobot.mini.voice.VoicePool;

import okhttp3.*;

public class RobotSocketClient extends Service {
    private WebSocket webSocket;
    private RobotSocketController robotController;
    public RobotSocketClient(){
        DanceWithMusicActivity act = new DanceWithMusicActivity();
        robotController = new RobotSocketController(act);
    }
    public RobotSocketClient(RobotSocketController robotController) {
        this.robotController = robotController;
    }

    public void connect() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://192.168.1.233:8000/websocket/ws")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                VoicePool vp = VoicePool.get();
                vp.playTTs("I am connected to the server", Priority.HIGH, null);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                VoicePool vp = VoicePool.get();
                vp.playTTs("I got a command", Priority.HIGH, null);
                if (robotController != null) {
                    robotController.handleCommand(text);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.i("Error", "Got error: ");
            }
        });
    }

    public void sendMessage(String msg) {
        if (webSocket != null) {
            boolean sent = webSocket.send(msg);
            System.out.println("Send message: " + msg + " success=" + sent);
        } else {
            System.err.println("Cannot send message, WebSocket null");
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Client closed");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
