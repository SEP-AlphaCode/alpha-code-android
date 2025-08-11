package com.ubtrobot.mini.sdkdemo.socket;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController {
    private static final String TAG = "RobotSocketController";

    private DanceWithMusicActivity danceWithMusicActivity;

    public RobotSocketController(DanceWithMusicActivity danceWithMusicActivity) {
        this.danceWithMusicActivity = danceWithMusicActivity;
    }

    public void handleCommand(String command) {
        if (command == null) return;

        try {
            JSONObject json = new JSONObject(command);
            String type = json.optString("type");
            JSONObject data = json.optJSONObject("data");

            Log.i(TAG, "Handling command type: " + type);

            switch (type) {
                case "dance-with-music":
                    if (danceWithMusicActivity != null) {
                        danceWithMusicActivity.JumpWithMusic(data);
                    }
                    break;
                case "say":
                    if (data != null) {
                        String textToSay = data.optString("text");
                        say(textToSay);
                    }
                    break;
                default:
                    Log.w(TAG, "Unknown command type: " + type);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON command: " + command, e);
        }
    }


    private void say(String text) {
        // TODO: Thêm code gọi TTS hoặc API nói chuyện
        Log.i(TAG, "Robot says: " + text);
    }
}
