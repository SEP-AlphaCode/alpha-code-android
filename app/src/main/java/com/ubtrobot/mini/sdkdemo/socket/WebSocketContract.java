package com.ubtrobot.mini.sdkdemo.socket;

public interface WebSocketContract {
    interface View {
        void onConnected();

        void onDisconnected();

        void onMessageReceived(String message);

        void onError(String error);
    }

    interface Presenter {
        void connect();

        void disconnect();

        void sendMessage(String message);

        boolean isConnected();

        void setView(View view);
    }
}
