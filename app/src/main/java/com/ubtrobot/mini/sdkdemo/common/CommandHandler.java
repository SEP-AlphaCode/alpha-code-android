package com.ubtrobot.mini.sdkdemo.common;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;

import org.json.JSONObject;

public class CommandHandler {
    private static final String TAG = "CommandHandler";

    private TakePicApiActivity takePicApiActivity;
    private ActionApi actionApi;
    private ExpressApi expressApi;
    private TTSManager tts;
    private DanceWithMusicActivity danceWithMusicActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CommandHandler() {
        this.danceWithMusicActivity = DanceWithMusicActivity.get();
        this.takePicApiActivity = TakePicApiActivity.get();
        this.actionApi = ActionApi.get();
        expressApi = ExpressApi.get();
        this.tts = new TTSManager(Utils.getContext().getApplicationContext());
    }

    public void handleCommand(String type, JSONObject data) {
        String text = data.optString("text");
        String code = data.optString("code");

        switch (type) {
            case "dance-with-music":
                handleWithDanceMusic(data);
                break;
            case "action":
                handleWithAction(code);
                break;
            case "expression":
                expressApi.doExpress(code);
                break;
            case "qr-code":
                handleQRCode(text);
                break;
            case "osmo-card":
                handleOsmoCard(text);
                break;
            default:
                handleDefault(text);
                break;
        }
    }

    private void handleWithAction(String actionCode) {
        if (actionCode != null) {
            actionApi.playAction(actionCode, new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    Log.i(TAG, "Action " + actionCode + " done!");
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.e(TAG, "Action " + actionCode + " failed: " + s);
                }
            });
        }
    }

    private void handleWithDanceMusic(JSONObject data) {
        if (danceWithMusicActivity != null) {
            danceWithMusicActivity.JumpWithMusic(data);
        }
    }

    private void handleQRCode(String text) {
        // Use default message if text is null or empty
        final String message = (text == null || text.trim().isEmpty())
                ? "Please show the QR code in front of me to take a picture. Now I will take the picture."
                : text;

        // Call TTS
        tts.doTTS(message, new TTSCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "TTS started: " + message);
            }

            @Override
            public void onDone() {
                Log.i(TAG, "Voice playback finished successfully");
                // Only take picture if a QR code text was provided
                if (message != null && !message.trim().isEmpty()) {
                    takePicApiActivity.takePicImmediately("qr-code");
                }
            }

            @Override
            public void onError() {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }


    private void handleOsmoCard(String text) {

        final String message = (text == null || text.trim().isEmpty())
                ? "Please place the OSMO card under my feet in my view. Now I will bend down to scan it."
                : text;
        tts.doTTS(message, new TTSCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "TTS started: " + message);
            }

            @Override
            public void onDone() {
                Log.i(TAG, "After voice played successfully");
                actionApi.playCustomizeAction("takelowpic", null);

                handler.postDelayed(() -> {
                    takePicApiActivity.takePicImmediately("osmo-card");
                }, 3000); // Delay 3 seconds before taking picture
            }

            @Override
            public void onError() {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }

    private void handleDefault(String text) {
        if (text != null) {
            tts.doTTS(text, new TTSCallback() {
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
        }
    }
}
