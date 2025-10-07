package com.ubtrobot.mini.sdkdemo.socket;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.common.CommandHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController implements WebSocketContract.View {
    private static final String TAG = "RobotSocketController";
    private CommandHandler commandHandler;

    public RobotSocketController() {
        this.commandHandler = new CommandHandler();

    }

    @Override
    public void onConnected() {
        Log.i(TAG, "WebSocket connected");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "WebSocket disconnected");
    }

    @Override
    public void onMessageReceived(String message) {
        if (message == null) return;

        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");
            JSONObject data = json.optJSONObject("data");

            Log.i(TAG, "Processing command - type: " + type + ", data: " + data);
            commandHandler.handleCommand(type, data, "en");

        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON command: " + message, e);
            onError("Invalid JSON format");
        }
    }

    @Override
    public void onBinaryMessageReceived(byte[] message) {
        Log.i(TAG, "Received binary message, length: " + message.length);

        // If your server sends protobuf responses, parse them here
        // For now, since you mentioned server responds with text,
        // we'll convert binary to string
        try {
            String textResponse = new String(message, "UTF-8");
            onMessageReceived(textResponse);
        } catch (Exception e) {
            Log.e(TAG, "Error converting binary message to string", e);
            onError("Failed to parse binary message");
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
    }
}
