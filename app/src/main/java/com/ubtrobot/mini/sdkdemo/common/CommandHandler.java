package com.ubtrobot.mini.sdkdemo.common;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.action.ActionExApi;
import com.ubtrobot.action.listeners.ActionExListener;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class CommandHandler {
    private static final String TAG = "CommandHandler";

    private TakePicApiActivity takePicApiActivity;
    private ActionApi actionApi;
    private ExpressApi expressApi;
    private TTSManager tts;
    private DanceWithMusicActivity danceWithMusicActivity;
    private ActionExApi actionExApi;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CommandHandler() {
        this.danceWithMusicActivity = DanceWithMusicActivity.get();
        this.takePicApiActivity = TakePicApiActivity.get();
        this.actionApi = ActionApi.get();
        this.expressApi = ExpressApi.get();
        this.actionExApi = ActionExApi.get();
        this.tts = new TTSManager(Utils.getContext().getApplicationContext());
    }

    public void handleCommand(String type, JSONObject data) {
        String text = data.optString("text");
        String code = data.optString("code");

        switch (type) {
            case "dance_with_music":
                handleWithDanceMusic(data);
                break;

            case "action":
                handleWithAction(code);
                break;

            case "expression":
                expressApi.doExpress(code);
                break;

            case "qr_code":
                handleQRCode(text);
                break;

            case "osmo_card":
                handleOsmoCard(text);
                break;

            case "extended_action":
                Log.i(TAG, "handleExtendedAction: " + data);

                if (data.has("actions")) {
                    JSONArray actions = data.optJSONArray("actions");
                    if (actions != null) {
                        executeActionsSequentially(actions, 0);
                    }
                } else {
                    // fallback for single action
                    String name = data.optString("name");
                    int step = data.optInt("step", 1);
                    handleExtendedAction(name, step, () -> {
                        Log.i(TAG, "Single extended action finished");
                    });
                }
                break;


            default:
                handleDefault(text);
                break;
        }
    }

    private void executeActionsSequentially(JSONArray actions, int index) {
        if (index >= actions.length()) {
            Log.i(TAG, "All extended actions finished");
            return;
        }

        JSONObject actionObj = actions.optJSONObject(index);
        if (actionObj != null) {
            String actName = actionObj.optString("name");
            int actStep = actionObj.optInt("step", 1);

            Log.i(TAG, "Executing action " + actName + " step=" + actStep);

            handleExtendedAction(actName, actStep, () -> {
                // After current action is done, execute the next one
                executeActionsSequentially(actions, index + 1);
            });
        } else {
            // If actionObj is null, skip to the next action
            executeActionsSequentially(actions, index + 1);
        }
    }



    private void handleExtendedAction(String name, int step, Runnable onComplete) {
        if (name == null || step <= 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ActionExListener listener = new ActionExListener() {
            @Override
            public void onActionCompleted() {
                Log.i(TAG, "Extended action " + name + " done!");
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onActonStarted() {

            }

            @Override
            public void onActionProgress(int i, int i1) {

            }

            @Override
            public void onActionFailure(int i, @NonNull String s) {
                Log.e(TAG, "Extended action " + name + " failed: " + s);
                if (onComplete != null) onComplete.run(); // still call onComplete on failure
            }
        };

        switch (name) {
            case "walk_forward":
                actionExApi.walkForward(step, Priority.HIGH, listener);
                break;
            case "walk_backward":
                actionExApi.walkBackward(step, Priority.HIGH, listener);
                break;
            case "turn_left":
                actionExApi.turnLeft(step, Priority.HIGH, listener);
                break;
            case "turn_right":
                actionExApi.turnRight(step, Priority.HIGH, listener);
                break;
            case "make_bows":
                actionExApi.makeBows(step, Priority.HIGH, listener);
                break;
            case "make_nods":
                actionExApi.makeNods(step, Priority.HIGH, listener);
                break;
            case "shake_heads":
                actionExApi.shakeHeads(step, Priority.HIGH, listener);
                break;
            case "slating_heads":
                actionExApi.slantingHeads(step, Priority.HIGH, listener);
                break;
            case "shake_hands":
                actionExApi.shakeHands(step, Priority.HIGH, listener);
                break;
            case "wave_hands":
                actionExApi.waveHands(step, Priority.HIGH, listener);
                break;
            case "make_press_ups":
                actionExApi.makePressUps(step, Priority.HIGH, listener);
                break;
            default:
                Log.w(TAG, "Unknown extended action: " + name);
                if (onComplete != null) onComplete.run();
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
