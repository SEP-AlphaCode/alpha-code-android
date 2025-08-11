package com.ubtrobot.mini.sdkdemo.socket;


import okhttp3.*;

public class RobotSocketClient {
    private WebSocket webSocket;
    private RobotSocketController robotController;

    public RobotSocketClient(RobotSocketController robotController) {
        this.robotController = robotController;
    }

    public void connect() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://192.168.1.233:8000/ws")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("WebSocket connected. Response: " + response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("Received: " + text);
                if (robotController != null) {
                    robotController.handleCommand(text);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WebSocket error: " + t.getMessage());
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
}
