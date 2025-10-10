package com.ubtrobot.mini.sdkdemo.socket;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.common.CommandHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController implements WebSocketContract.View {
    private static final String TAG = "RobotSocketController";
    private CommandHandler commandHandler;
    private RobotSocketManager socketManager;

    public RobotSocketController(RobotSocketManager socketManager) {
        this.socketManager = socketManager;
        this.commandHandler = new CommandHandler();
        // Set the socket manager for handlers that need to send responses
        this.commandHandler.setSocketManager(socketManager);
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
            String lang = json.optString("lang", "en");

            Log.i(TAG, "Processing command - type: " + type + ", data: " + data);
            commandHandler.handleCommand(type, data, lang);

        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON command: " + message, e);
            onError("Invalid JSON format");
        }
    }

    @Override
    public void onBinaryMessageReceived(byte[] message) {
        Log.i(TAG, "Received binary message, length: " + message.length);

        // Try to parse as protobuf first
        RobotRequestProto.RobotRequest request = ProtobufConverter.bytesToRequest(message);
        if (request != null) {
            // Handle protobuf response
            handleProtobufResponse(request);
        } else {
            // Fall back to text conversion
            try {
                String textResponse = new String(message, "UTF-8");
                onMessageReceived(textResponse);
            } catch (Exception e) {
                Log.e(TAG, "Error converting binary message to string", e);
                onError("Failed to parse binary message");
            }
        }
    }

    private void handleProtobufResponse(RobotRequestProto.RobotRequest response) {
        // If your server starts sending protobuf responses, handle them here
        Log.d(TAG, "Received protobuf response - Type: " + response.getType());
        // Convert protobuf response to your command format if needed
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
    }
}
