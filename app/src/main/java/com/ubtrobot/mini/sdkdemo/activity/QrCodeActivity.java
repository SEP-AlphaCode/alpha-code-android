package com.ubtrobot.mini.sdkdemo.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QrCodeActivity {
    private ActionApi actionApi;
    private ExpressApi expressApi;
    private MouthLedApi mouthLedApi;
    private TTSManager ttsManager;
    private static final String TAG = "QrCodeActivity";
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static QrCodeActivity get() {
        return QrCodeActivity.Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static QrCodeActivity _api = new QrCodeActivity();
    }

    public void DoActivity(String jsonString, String name) {
        try {
            initRobot();
            JSONObject jsonObject = new JSONObject(jsonString);
            playScriptFromJson(jsonObject, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initRobot() {
        this.ttsManager = TTSManager.getInstance();
        this.actionApi = ActionApi.get();
        this.expressApi = ExpressApi.get();
        this.mouthLedApi = MouthLedApi.get();
    }


    private void playScriptFromJson(JSONObject script, String name) {
        try {
            JSONArray actions = script.getJSONArray("activities");
            String preVoice = "Start play " + name;
            String afterVoice = "Finish play " + name;
            long totalDuration = script.getLong("total_duration");

            // Delay for waiting TTS ready
            handler.postDelayed(() -> {
            ttsManager.doTTS(preVoice, new TTSCallback() {
                @Override
                public void onStart() {
                    Log.i(TAG, "Playing pre voice: " + preVoice);
                    LogManager.log(LogLevel.INFO, TAG,"Playing pre voice: " + preVoice);
                    LogManager.log(LogLevel.INFO, TAG,"Start play " + name);
                }

                @Override
                public void onDone() {
                    Log.i(TAG, "Pre voice played successfully");
                    // Delay 1s then start playing actions
                    handler.postDelayed(() -> {
                        Log.i(TAG, "Playing script with " + actions.length() + " actions");
                        LogManager.log(LogLevel.INFO, TAG,"Playing script with " + actions.length() + " actions");

                        for (int i = 0; i < actions.length(); i++) {
                            JSONObject action;
                            try {
                                action = actions.getJSONObject(i);
                                String actionId = action.getString("action_id");
                                double startTime = action.getDouble("start_time");
                                double duration = action.getDouble("duration");
                                String type = action.getString("action_type");


                                JSONObject colorObj = action.optJSONObject("color");
                                int a = 0, r = 255, g = 255, b = 255;
                                if (colorObj != null) {
                                    a = colorObj.optInt("a", 0);
                                    r = colorObj.optInt("r", 255);
                                    g = colorObj.optInt("g", 255);
                                    b = colorObj.optInt("b", 255);
                                }
                                int finalA = a, finalR = r, finalG = g, finalB = b;

                                handler.postDelayed(() -> {
                                    Log.i(TAG, "Executing action: " + actionId);
                                    LogManager.log(LogLevel.INFO, TAG,"Executing action: " + actionId);

                                    try {
                                        mouthLedApi.startNormalModel(Color.argb(finalA, finalR, finalG, finalB),
                                                (int) (duration), Priority.NORMAL, null);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error setting LED color", e);
                                        LogManager.log(LogLevel.ERROR, TAG,"Error setting LED color: " + e.getMessage());
                                    }

                                    switch (type) {
                                        case "dance":
                                            actionApi.playAction(actionId, new ResponseListener<Void>() {
                                                @Override
                                                public void onResponseSuccess(Void aVoid) {
                                                    Log.i(TAG, "Action " + actionId + " completed successfully");
                                                    LogManager.log(LogLevel.INFO, TAG,"Action " + actionId + " completed successfully");
                                                }

                                                @Override
                                                public void onFailure(int errorCode, @NonNull String errorMessage) {
                                                    Log.e(TAG, "Action " + actionId + " failed: " + errorMessage);
                                                    LogManager.log(LogLevel.ERROR, TAG,"Action " + actionId + " failed: " + errorMessage);
                                                }
                                            });
                                            break;
                                        case "expression":
                                            try {
                                                expressApi.doExpress(actionId, 1, true, Priority.HIGH, new AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart() {
                                                        Log.i(TAG, "Expression " + actionId + " started");
                                                        LogManager.log(LogLevel.INFO, TAG,"Expression " + actionId + " started");
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(int i) {
                                                        Log.i(TAG, "doExpress表情执行结束!");
                                                        LogManager.log(LogLevel.INFO, TAG,"Expression " + actionId + " ended");
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(int loopNumber) {
                                                        Log.i(TAG, "Expression: " + actionId + ", With loop: " + loopNumber);
                                                        LogManager.log(LogLevel.INFO, TAG,"Expression: " + actionId + ", With loop: " + loopNumber);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error executing expression " + actionId, e);
                                                LogManager.log(LogLevel.ERROR, TAG,"Error executing expression " + actionId + ": " + e.getMessage());
                                            }
                                            break;
                                        default:
                                            Log.w(TAG, "Unknown action type: " + type + " for action: " + actionId);
                                            LogManager.log(LogLevel.WARN, TAG,"Unknown action type: " + type + " for action: " + actionId);
                                            break;
                                    }

                                }, (long) (startTime));

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing action JSON", e);
                                LogManager.log(LogLevel.ERROR, TAG,"Error parsing action JSON: " + e.getMessage());
                            }
                        }

                        // Sau khi toàn bộ action xong + delay 1s → nói afterVoice
                        handler.postDelayed(() -> {
                            ttsManager.doTTS(afterVoice);
                            LogManager.log(LogLevel.INFO, TAG,"Playing after voice: " + afterVoice);
                        }, (long) (totalDuration) + 1000);

                    }, 1000); // Delay 1s sau khi nói preVoice
                }

                @Override
                public void onError() {
                    Log.i(TAG, "Error playing pre voice");
                    LogManager.log(LogLevel.ERROR, TAG,"Error playing pre voice");
                }
            });
            }, 500);

        } catch (Exception e) {
            Log.e(TAG, "Error in playScriptFromJson", e);
            LogManager.log(LogLevel.ERROR, TAG,"Error in playScriptFromJson: " + e.getMessage());
        }
    }
}
