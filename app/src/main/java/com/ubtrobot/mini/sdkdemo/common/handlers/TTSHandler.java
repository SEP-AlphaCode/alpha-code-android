package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.tts.EnTTSManager;

public class TTSHandler {
    private static final String TAG = "TTSHandler";
    private EnTTSManager tts;

    public TTSHandler() {
        this.tts = EnTTSManager.getInstance();
    }

    public void handleDefault(String text) {
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
