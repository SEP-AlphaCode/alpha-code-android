package com.ubtrobot.mini.sdkdemo.custom.tts;

import android.content.Context;

public interface TTS {
    void doTTS(String text, TTSCallback callback);
    void doTTS(String text);
    void init(Context context);
    void shutdown();
    boolean isReady();
}