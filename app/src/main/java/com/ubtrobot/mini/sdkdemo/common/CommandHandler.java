package com.ubtrobot.mini.sdkdemo.common;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.common.handlers.ActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.CameraHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.DanceHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExpressionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExtendedActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ObjectDetectHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SkillHandler;
import com.ubtrobot.mini.sdkdemo.custom.CameraPreviewCapture;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSHandler;
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
    private CameraPreviewCapture captureObject;
    private ObjectDetectHandler objectDetectHandler;

    public CommandHandler() {
        // Initialize all handlers
        this.actionHandler = new ActionHandler();
        this.extendedActionHandler = new ExtendedActionHandler();
        this.skillHandler = new SkillHandler();
        this.expressionHandler = new ExpressionHandler();
        this.danceHandler = new DanceHandler();
        this.cameraHandler = new CameraHandler();
        this.captureObject = new CameraPreviewCapture(Utils.getContext().getApplicationContext());
        this.objectDetectHandler = new ObjectDetectHandler();
    }

    public void handleCommand(String type, String lang, JSONObject data) {
        String text = data.optString("text");
        String code = data.optString("code");
        Log.i(TAG, text);
        Log.i(TAG, lang);
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
                cameraHandler.handleQRCode(text, lang);
                break;

            case "osmo_card":
                cameraHandler.handleOsmoCard(text, lang);
                break;

            case "extended_action":
                extendedActionHandler.handleExtendedAction(data);
                break;

            case "object_detect_start":
                TTSHandler.doTTS(text, lang, new TTSCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onDone() {
                        captureObject.openCamera(lang, (file, lang) -> {
                            objectDetectHandler.handleDetect(file, lang);
                        });
                    }

                    @Override
                    public void onError() {
                    }
                });
                break;
            case "object_detect_result":
                TTSHandler.doTTS(text, lang);
            default:
                TTSHandler.doTTS(text, lang);
                break;
        }
    }
}
