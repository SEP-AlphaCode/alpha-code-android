package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;

public class TTSHandler {
    private static final String TAG = "TTSHandler";
    private TTSManager tts;

    public TTSHandler() {
        this.tts = TTSManager.getInstance();
    }

    public void handleDefault(String text) {
        if (text != null) {
            tts.doTTS(text, new TTSCallback() {
                @Override
                public void onStart() {
                    Log.i(TAG, "TTS started: " + text);
                    LogManager.log(LogLevel.INFO, TAG, "TTS started: " + text);
                }

                @Override
                public void onDone() {
                    Log.i(TAG, "After TTS successfully");
                }

                @Override
                public void onError() {
                    Log.e(TAG, "Error playing TTS: " + text);
                    LogManager.log(LogLevel.ERROR, TAG, "Error playing TTS: " + text);
                }
            });
        }
    }
}
