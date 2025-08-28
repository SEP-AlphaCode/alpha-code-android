package com.ubtrobot.mini.sdkdemo.socket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.mini.sdkdemo.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RobotSocketController {
    private static final String TAG = "RobotSocketController";

    private DanceWithMusicActivity danceWithMusicActivity;
    private TakePicApiActivity takePicApiActivity;
    private TTSManager tts;
    private ActionApiActivity actionApiActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RobotSocketController() {
        initRbot();
    }

    private void initRbot(){
        this.danceWithMusicActivity = DanceWithMusicActivity.get();
        this.takePicApiActivity = TakePicApiActivity.get();
        this.actionApiActivity = ActionApiActivity.get();
        this.tts = new TTSManager(Utils.getContext().getApplicationContext());
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

                case "osmo-card":
                    if (data != null) {
                        String text = data.optString("text");
                        tts.doTTS(text, new TTSManager.TTSCallback() {
                            @Override
                            public void onStart() {
                                Log.i(TAG, "TTS started: " + text);
                            }

                            @Override
                            public void onDone() {
                                Log.i(TAG, "After voice played successfully");
                                actionApiActivity.playActionToTakeQR("takelowpic");

                                handler.postDelayed(() -> {
                                    takePicApiActivity.takePicImmediately("osmo-card");
                                }, 3000);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "Error playing TTS: " + text);
                            }
                        });
                    }
                    break;

                case "qr-code":
                    if (data != null) {
                        String text = data.optString("text");
                        tts.doTTS(text, new TTSManager.TTSCallback() {
                            @Override
                            public void onStart() {
                                Log.i(TAG, "TTS started: " + text);
                            }

                            @Override
                            public void onDone() {
                                Log.i(TAG, "After voice played successfully");

                                handler.postDelayed(() -> {
                                    takePicApiActivity.takePicImmediately("qr-code");
                                }, 3000);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "Error playing TTS: " + text);
                            }
                        });
                    }
                    break;

                default:
                    String text = data.optString("text");
                    tts.doTTS(text, new TTSManager.TTSCallback() {
                        @Override
                        public void onStart() {
                            Log.i(TAG, "TTS started: " + text);
                        }

                        @Override
                        public void onDone() {
                            Log.i(TAG, "After voice played successfully");
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "Error playing TTS: " + text);
                        }
                    });
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON command: " + command, e);
        }
    }
}
