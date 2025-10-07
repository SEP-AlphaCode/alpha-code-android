package com.ubtrobot.mini.sdkdemo.socket;

// WebSocketContract.java - Add binary support
public interface WebSocketContract {
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
        boolean isConnected();
        void setView(View view);
        static RobotSocketManager getInstance(String serverUrl, String robotSerial) {
            return RobotSocketManager.getInstance(serverUrl, robotSerial);
        }

        static RobotSocketManager getInstance() {
            return RobotSocketManager.getInstance();
        }

        static boolean isInitialized() {
            return RobotSocketManager.isInitialized();
        }
    }
}