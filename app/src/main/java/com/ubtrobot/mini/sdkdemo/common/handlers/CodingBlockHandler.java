package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;

import org.json.JSONArray;
import org.json.JSONObject;

public class CodingBlockHandler {
    private static final String TAG = "CodingBlockHandler";

    private final ActionHandler actionHandler;
    private final ExpressionHandler expressionHandler;
    private final TTSHandler ttsHandler;
    private final ExtendedActionHandler extendedActionHandler;

    public CodingBlockHandler() {
        this.actionHandler = new ActionHandler();
        this.expressionHandler = new ExpressionHandler();
        this.ttsHandler = new TTSHandler();
        this.extendedActionHandler = new ExtendedActionHandler();
    }

    public void executeCodingBlock(JSONArray actions, int index, String lang) {
        if (index >= actions.length()) {
            Log.i(TAG, "All coding block actions done!");
            return;
        }

        JSONObject action = actions.optJSONObject(index);
        if (action == null) {
            executeCodingBlock(actions, index + 1, lang);
            return;
        }

        String type = action.optString("type");
        String code = action.optString("code");
        String text = action.optString("text");

        switch (type) {
            case "tts":
                ttsHandler.doTTS(text, lang, new TTSCallback() {
                    @Override
                    public void onStart() {}

                    @Override
                    public void onDone() {
                        executeCodingBlock(actions, index + 1, lang);
                    }

                    @Override
                    public void onError() {
                        executeCodingBlock(actions, index + 1, lang);
                    }
                });
                break;

            case "expression":
                expressionHandler.handleExpression(code);
                executeCodingBlock(actions, index + 1, lang);
                break;

            case "action":
                actionHandler.handleAction(code);
                executeCodingBlock(actions, index + 1, lang);
                break;

            case "extended_action":
                JSONObject extData = action.optJSONObject("data");
                if (extData != null) {
                    extendedActionHandler.handleExtendedAction(extData);
                }
                executeCodingBlock(actions, index + 1, lang);
                break;

            default:
                Log.w(TAG, "Unknown coding block action: " + type);
                executeCodingBlock(actions, index + 1, lang);
                break;
        }
    }
}
