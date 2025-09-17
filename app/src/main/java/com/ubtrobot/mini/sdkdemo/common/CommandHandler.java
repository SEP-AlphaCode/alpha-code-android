package com.ubtrobot.mini.sdkdemo.common;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.common.handlers.ActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.CameraHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.DanceHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExpressionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExtendedActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SkillHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.custom.CameraPreviewCapture;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;

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
    private TTSHandler ttsHandler;
    private SystemHandler systemHandler;
    public CommandHandler() {
        // Initialize all handlers
        this.actionHandler = new ActionHandler();
        this.extendedActionHandler = new ExtendedActionHandler();
        this.skillHandler = new SkillHandler();
        this.expressionHandler = new ExpressionHandler();
        this.danceHandler = new DanceHandler();
        this.cameraHandler = new CameraHandler();
        this.ttsHandler = new TTSHandler();
        this.systemHandler = SystemHandler.get();
    }

    public void handleCommand(String type, JSONObject data, String lang) {
        String text = data.optString("text");
        String code = data.optString("code");

        switch (type) {
            case "status_req":
                systemHandler.sendRobotStatus();
                break;
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
                ttsHandler.doTTS(text, lang, new TTSCallback() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onDone() {
                        CameraPreviewCapture previewCapture = new CameraPreviewCapture(Utils.getContext().getApplicationContext());
                        previewCapture.openCamera(lang);
                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            default:
                ttsHandler.doTTS(text, lang);
                break;
        }
    }
}
