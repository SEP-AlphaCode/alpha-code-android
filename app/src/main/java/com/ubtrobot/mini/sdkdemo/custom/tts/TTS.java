package com.ubtrobot.mini.sdkdemo.custom.tts;

import android.content.Context;

public interface TTS {
    void init(Context context);

    void doTTS(String text);

    void doTTS(String text, TTSCallback callback);

    void shutdown();

    boolean isReady();
}
