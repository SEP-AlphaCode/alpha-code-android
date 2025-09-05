package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;

public class TTSHandler {
    private static final String TAG = "TTSHandler";
    private TTSManager tts;

    public TTSHandler() {
        this.tts = new TTSManager(Utils.getContext().getApplicationContext());
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
