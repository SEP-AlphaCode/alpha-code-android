package com.ubtrobot.mini.sdkdemo.socket;

import android.util.Log;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController {
    private static final String TAG = "RobotSocketController";
    private CommandHandler commandHandler;

    public RobotSocketController() {
        this.commandHandler = new CommandHandler();

    }

    public void handleCommand(String command) {
        if (command == null) return;

        try {
            JSONObject json = new JSONObject(command);
            String type = json.optString("type");
            JSONObject data = json.optJSONObject("data");
            commandHandler.handleCommand(type, data);

        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON command: " + command, e);
        }
    }
}
