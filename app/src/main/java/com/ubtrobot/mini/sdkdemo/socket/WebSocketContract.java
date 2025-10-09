package com.ubtrobot.mini.sdkdemo.socket;

// WebSocketContract.java - Add binary support
public interface WebSocketContract {
    interface ResultCallback {
        void onSuccess();
        void onFailure(String error);
    }
    interface View {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(String message);
        void onBinaryMessageReceived(byte[] message); // Add this
        void onError(String error);
    }

    interface Presenter {
        void connect();
        void disconnect();
        void sendMessage(String message);
        void sendBinaryMessage(byte[] message); // Add this
        void sendMessage(String message, ResultCallback callback);
        void sendBinaryMessage(byte[] message, ResultCallback callback); // Add this
        boolean isConnected();
        void setView(View view);
    }
}