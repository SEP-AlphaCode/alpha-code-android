package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.content.Context;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.custom.tts.EnglishTTS;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.tts.VietnameseTTS;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;

public class TTSHandler {
    private static final String TAG = "TTSHandler";
    private static EnglishTTS englishTTS;
    private static VietnameseTTS vietnameseTTS;

    private static TTSCallback defaultCallback(String text) {
        return new TTSCallback() {
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
        };
    }

    public static void init(Context context) {
        englishTTS = EnglishTTS.getInstance(context);
        vietnameseTTS = VietnameseTTS.getInstance(context);
        Log.i(TAG, "Done init");
    }

    public void doTTS(String text, String lang) {
        if (text == null) return;
        if (lang.equals("en")) {
            englishTTS.doTTS(text, defaultCallback(text));
        } else {
            vietnameseTTS.doTTS(text, defaultCallback(text));
        }
    }
    public void doTTS(String text, String lang, TTSCallback callback) {
        if (text == null) return;
        if (lang.equals("en")) {
            englishTTS.doTTS(text, callback);
        } else {
            vietnameseTTS.doTTS(text, callback);
        }
    }
}
