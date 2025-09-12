package com.ubtrobot.mini.sdkdemo.common;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.common.handlers.ActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.CameraHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.DanceHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExpressionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExtendedActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SkillHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.custom.CameraPreviewCapture;
import com.ubtrobot.mini.sdkdemo.custom.tts.EnTTSManager;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

import org.json.JSONObject;

public class CommandHandler {
    private static final String TAG = "CommandHandler";

    // Handler instances
    private ActionHandler actionHandler;
    private ExtendedActionHandler extendedActionHandler;
    private SkillHandler skillHandler;
    private ExpressionHandler expressionHandler;
    private DanceHandler danceHandler;
    private CameraHandler cameraHandler;
    private VoicePool vp = VoicePool.get();

    public CommandHandler() {
        // Initialize all handlers
        this.actionHandler = new ActionHandler();
        this.extendedActionHandler = new ExtendedActionHandler();
        this.skillHandler = new SkillHandler();
        this.expressionHandler = new ExpressionHandler();
        this.danceHandler = new DanceHandler();
        this.cameraHandler = new CameraHandler();
    }

    public void handleCommand(String type, JSONObject data) {
        String text = data.optString("text");
        String code = data.optString("code");

        switch (type) {
            case "dance_with_music":
                danceHandler.handleDanceWithMusic(data);
                break;

            case "skill":
                skillHandler.handleSkill();
                break;

            case "skill_helper":
                skillHandler.handleSkillHelper(code);
                break;

            case "action":
                actionHandler.handleAction(code);
                break;

            case "expression":
                expressionHandler.handleExpression(code);
                break;

            case "qr_code":
                cameraHandler.handleQRCode(text);
                break;

            case "osmo_card":
                cameraHandler.handleOsmoCard(text);
                break;

            case "extended_action":
                extendedActionHandler.handleExtendedAction(data);
                break;

            case "object_detect_start":
                vp.playTTs(text, Priority.HIGH, new VoiceListener() {

                    @Override
                    public void onCompleted() {
                        CameraPreviewCapture previewCapture = new CameraPreviewCapture(Utils.getContext().getApplicationContext());
                        previewCapture.openCamera();
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });
                break;

            default:
                //ttsHandler.handleDefault(text);
                vp.playTTs(text, Priority.HIGH, new VoiceListener() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "TTS completed for text: " + text);
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.e(TAG, "TTS error for text: " + text + ", error: " + s);
                    }
                });
                break;
        }
    }
}
