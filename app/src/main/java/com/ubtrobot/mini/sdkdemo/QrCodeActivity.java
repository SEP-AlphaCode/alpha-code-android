package com.ubtrobot.mini.sdkdemo;

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
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QrCodeActivity {
    private ActionApi actionApi;
    private ExpressApi expressApi;
    private MouthLedApi mouthLedApi;
    private VoicePool voicePool;

    private static final String TAG = "QrCodeActivity";
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static QrCodeActivity get() {
        return QrCodeActivity.Holder._api;
    }

    public void DoActivity(String jsonString) {
        initRobot();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            playScriptFromJson(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initRobot() {
        actionApi = ActionApi.get();
        expressApi = ExpressApi.get();
        mouthLedApi = MouthLedApi.get();
        voicePool = VoicePool.get();
    }



    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static QrCodeActivity _api = new QrCodeActivity();
    }

    private void playScriptFromJson(JSONObject script) {
        try {
            JSONArray actions = script.getJSONObject("activity").getJSONArray("actions");
            String preVoice = script.getString("pre-voice");
            String afterVoice = script.getString("after-voice");
            long totalDuration = script.getLong("total-duration");

            voicePool.playTTs(preVoice, Priority.MAXHIGH, new VoiceListener() {
                @Override
                public void onCompleted() {
                    Log.i(TAG, "Pre voice played successfully");

                    // Delay 1s then start playing actions
                    handler.postDelayed(() -> {
                        Log.i(TAG, "Playing script with " + actions.length() + " actions");

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

                                    try {
                                        mouthLedApi.startNormalModel(Color.argb(finalA, finalR, finalG, finalB),
                                                (int) (duration * 1000), Priority.NORMAL, null);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error setting LED color", e);
                                    }

                                    switch (type) {
                                        case "dance":
                                            actionApi.playAction(actionId, new ResponseListener<Void>() {
                                                @Override
                                                public void onResponseSuccess(Void aVoid) {
                                                    Log.i(TAG, "Action " + actionId + " completed successfully");
                                                }

                                                @Override
                                                public void onFailure(int errorCode, @NonNull String errorMessage) {
                                                    Log.e(TAG, "Action " + actionId + " failed: " + errorMessage);
                                                }
                                            });
                                            break;
                                        case "expression":
                                            try {
                                                expressApi.doExpress(actionId, 1, true, Priority.HIGH, new AnimationListener() {
                                                    @Override public void onAnimationStart() {
                                                        Log.i(TAG, "doExpress开始执行表情!");
                                                    }
                                                    @Override public void onAnimationEnd(int i) {
                                                        Log.i(TAG, "doExpress表情执行结束!");
                                                    }
                                                    @Override public void onAnimationRepeat(int loopNumber) {
                                                        Log.i(TAG, "doExpress重复执行表情,重复次数:" + loopNumber);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.e(TAG, "Error executing expression " + actionId, e);
                                            }
                                            break;
                                        default:
                                            Log.w(TAG, "Unknown action type: " + type + " for action: " + actionId);
                                            break;
                                    }

                                }, (long) (startTime * 1000));

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing action JSON", e);
                            }
                        }

                        // Sau khi toàn bộ action xong + delay 1s → nói afterVoice
                        handler.postDelayed(() -> {
                            voicePool.playTTs(afterVoice, Priority.MAXHIGH, new VoiceListener() {
                                @Override
                                public void onCompleted() {
                                    Log.i(TAG, "After voice played successfully");
                                }

                                @Override
                                public void onError(int i, String s) {
                                    Log.e(TAG, "Error playing after voice: " + s);
                                }
                            });
                        }, (long) (totalDuration * 1000) + 1000);

                    }, 1000); // Delay 1s sau khi nói preVoice
                }

                @Override
                public void onError(int i, String s) {
                    Log.i(TAG, "Error Code: " + i + ", Error Message: " + s);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in playScriptFromJson", e);
        }
    }
}
