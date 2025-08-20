package com.ubtrobot.mini.sdkdemo.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.mini.sdkdemo.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController {
    private static final String TAG = "RobotSocketController";

    private DanceWithMusicActivity danceWithMusicActivity;
    private TakePicApiActivity takePicApiActivity;
    private VoicePool voicePool;
    private ActionApiActivity actionApiActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    public RobotSocketController(DanceWithMusicActivity danceWithMusicActivity, TakePicApiActivity takePicApiActivity, ActionApiActivity actionApiActivity) {
        this.danceWithMusicActivity = danceWithMusicActivity;
        this.takePicApiActivity = takePicApiActivity;
        this.actionApiActivity = actionApiActivity;
        // Initialize voicePool once in constructor instead of every handleCommand call
        this.voicePool = VoicePool.get();
    }

    public void handleCommand(String command) {
        if (command == null) return;
        // Remove initRobot() call
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
                case "osmo-card":
                    if (data != null) {
                        String tts = "Start capture OSMO card";
                        voicePool.playTTs(tts, Priority.MAXHIGH, new VoiceListener() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "After voice played successfully");
                                actionApiActivity.playActionToTakeQR("takelowpic");

                                handler.postDelayed(() -> {
                                    takePicApiActivity.takePicImmediately("osmo-card");
                                }, 3000); // Delay 3 seconds before taking picture
                            }

                            @Override
                            public void onError(int i, String s) {
                                Log.e(TAG, "Error playing after voice: " + s);
                            }
                        });

                    }
                    break;
                case "qr-code":
                    if (data != null) {
                        String tts = data.optString("text");
                        voicePool.playTTs(tts, Priority.MAXHIGH, new VoiceListener() {
                            @Override
                            public void onCompleted() {
                                Log.i(TAG, "After voice played successfully");
                                actionApiActivity.playActionToTakeQR("takelowpic");

                                handler.postDelayed(() -> {
                                    takePicApiActivity.takePicImmediately("qr-code");
                                }, 3000); // Delay 3 seconds before taking picture
                            }

                            @Override
                            public void onError(int i, String s) {
                                Log.e(TAG, "Error playing after voice: " + s);
                            }
                        });

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
}
