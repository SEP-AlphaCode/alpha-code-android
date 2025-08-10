package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.mini.sdkdemo.utils.RobotUtils;
import com.ubtrobot.mini.voice.MiniMediaPlayer;
import com.ubtrobot.mini.voice.protos.VoiceProto;


import org.json.JSONArray;
import org.json.JSONObject;

public class DanceWithMusicActivity extends Activity {

    private MiniMediaPlayer miniPlayer;
    private ActionApi actionApi;
    private ExpressApi expressApi;
    private MouthLedApi mouthLedApi;
    private static final String TAG = "DanceWithMusic";
    private final Handler handler = new Handler(Looper.getMainLooper());

    private void initRobot() {
        actionApi = ActionApi.get();
        expressApi = ExpressApi.get();
        mouthLedApi = MouthLedApi.get();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dance_with_music);

        MasterContext context = RobotUtils.getMasterContext();
        VoiceProto.Source source = RobotUtils.getVoiceProtoSource();

        // URL nhạc từ JSON
        String audioPath = "https://storage.googleapis.com/alphamini-music-configs/music/starboy_dance.wav?X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=alpha-mini-stt-service%40alphamini-465103.iam.gserviceaccount.com%2F20250809%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20250809T082627Z&X-Goog-Expires=604800&X-Goog-SignedHeaders=host&X-Goog-Signature=3665cfb6e9aec45b71b0561ec34474824b28d379a1c2e5d2f4d9f86452cd5dcf5332453b62f382fcf4dc1a74c528c808e43b90de88c8f6f75efc97ee38c0f24bd0c29c00f80f15a059e9910bd132e3ae3c969a7bfb1e7337c490c334aa7f59fb2a8d631117cc3ac51481f6e5cb441fe71e2b2ed158e66ceca75fc63408332daa8cd735653ba5974b8ee1aa8c74da7d744f4647321c43542db8ccb0834fbb760b7727e358c34cbcc1e3224a09091e62e0742dd124ce33426dc9e52381b4ef7af94aef71bbcd7ec4db6acfadcbcf026c1b3234f63922a13a321f5f069dd4332e93958a222dab780be216d6dce78f73dd7444368a25773b2746e12fb37765f2742e";

        initRobot();
        initPlayer(context, source, audioPath);
    }

    private void initPlayer(MasterContext context, VoiceProto.Source source, String audioPath) {
        try {
            miniPlayer = MiniMediaPlayer.create(context, source);
            miniPlayer.setDataSource(audioPath);

            miniPlayer.setOnPreparedListener(mp -> {
                Log.i(TAG, "Media ready, start playing");
                mp.start();
                // Chạy script JSON khi nhạc bắt đầu
                try {
                    String jsonScript = loadScriptJson();
                    playScriptFromJson(new JSONObject(jsonScript));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON", e);
                }
            });

            miniPlayer.setOnCompletionListener(mp -> Log.i(TAG, "Playback completed"));

            miniPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error playing media: " + what + ", " + extra);
                return true;
            });

            miniPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing player", e);
        }
    }

    private void playScriptFromJson(JSONObject script) {
        try {
            JSONArray actions = script.getJSONObject("activity").getJSONArray("actions");

            Log.i(TAG, "Playing script with " + actions.length() + " actions");

            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                String actionId = action.getString("action_id");
                double startTime = action.getDouble("start_time");
                double duration = action.getDouble("duration");
                String type = action.getString("action_type");

                // Đọc màu ARGB từ JSON
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
                    Log.i(TAG, "Executing action: " + actionId + " at time: " + startTime + " duration: " + duration);

                    // Set màu LED với thời gian duration
                    try {
                        mouthLedApi.startNormalModel(Color.argb(finalA, finalR, finalG, finalB),
                                (int) (duration * 1000), Priority.NORMAL, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting LED color", e);
                    }

                    // Chạy action dựa trên type
                    switch (type) {
                        case "dance":
                            actionApi.playAction(actionId ,new ResponseListener<Void>() {
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
                                expressApi.doExpress(actionId, 1, true, Priority.NORMAL);
                            } catch (Exception e) {
                                Log.e(TAG, "Error executing expression " + actionId, e);
                            }
                            break;
                        default:
                            Log.w(TAG, "Unknown action type: " + type + " for action: " + actionId);
                            break;
                    }

                }, (long) (startTime * 1000));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in playScriptFromJson", e);
        }
    }

    private String loadScriptJson() {
        return "{\n" +
                "  \"music_info\": {\n" +
                "    \"name\": \"mixed_dance_action_expression\",\n" +
                "    \"music_file_url\": \"https://storage.googleapis.com/alphamini-music-configs/music/mixed_show.wav\",\n" +
                "    \"duration\": 60\n" +
                "  },\n" +
                "  \"activity\": {\n" +
                "    \"actions\": [\n" +
                "      { \"action_id\": \"dance_0001en\", \"start_time\": 0.0,  \"duration\": 6.0, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 255, \"g\": 0,   \"b\": 0 } },\n" +
                "      { \"action_id\": \"wakeup\",      \"start_time\": 1.0,  \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 255, \"b\": 0 } },\n" +
                "      { \"action_id\": \"dance_0002en\", \"start_time\": 6.0,  \"duration\": 6.5, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 0,   \"b\": 255 } },\n" +
                "      { \"action_id\": \"emo_016\",      \"start_time\": 14.5, \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 255, \"g\": 255, \"b\": 0 } },\n" +
                "      { \"action_id\": \"dance_0006en\", \"start_time\": 12.5, \"duration\": 9.0, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 255, \"g\": 0,   \"b\": 255 } },\n" +
                "      { \"action_id\": \"action_016\",   \"start_time\": 21.5, \"duration\": 3.0, \"action_type\": \"action\", \"color\": { \"a\": 0, \"r\": 255, \"g\": 165, \"b\": 0 } },\n" +
                "      { \"action_id\": \"emo_007\",      \"start_time\": 24.5, \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 255, \"b\": 255 } },\n" +
                "      { \"action_id\": \"dance_0009en\", \"start_time\": 24.5, \"duration\": 7.0, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 128, \"g\": 0,   \"b\": 128 } },\n" +
                "      { \"action_id\": \"codemao12\",      \"start_time\": 31.5, \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 128, \"b\": 0 } },\n" +
                "      { \"action_id\": \"action_004\",   \"start_time\": 33.5, \"duration\": 3.0, \"action_type\": \"action\", \"color\": { \"a\": 0, \"r\": 128, \"g\": 128, \"b\": 0 } },\n" +
                "      { \"action_id\": \"dance_0011en\", \"start_time\": 36.5, \"duration\": 5.0, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 0,   \"b\": 128 } },\n" +
                "      { \"action_id\": \"emo_015\",      \"start_time\": 41.5, \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 255, \"g\": 192, \"b\": 203 } },\n" +
                "      { \"action_id\": \"dance_0005en\", \"start_time\": 43.5, \"duration\": 5.0, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 210, \"g\": 105, \"b\": 30 } },\n" +
                "      { \"action_id\": \"action_018\",   \"start_time\": 48.5, \"duration\": 3.0, \"action_type\": \"action\", \"color\": { \"a\": 0, \"r\": 0,   \"g\": 255, \"b\": 127 } },\n" +
                "      { \"action_id\": \"emo_025\",      \"start_time\": 51.5, \"duration\": 10.0, \"action_type\": \"expression\", \"color\": { \"a\": 0, \"r\": 75,  \"g\": 0,   \"b\": 130 } },\n" +
                "      { \"action_id\": \"dance_0004en\", \"start_time\": 53.5, \"duration\": 6.5, \"action_type\": \"dance\", \"color\": { \"a\": 0, \"r\": 173, \"g\": 255, \"b\": 47 } }\n" +
                "    ]\n" +
                "  }\n" +
                "}\n";
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (miniPlayer != null) {
            miniPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
