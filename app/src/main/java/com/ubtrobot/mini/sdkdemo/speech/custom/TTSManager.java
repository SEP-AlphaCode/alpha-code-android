package com.ubtrobot.mini.sdkdemo.speech.custom;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTSManager {
    private TextToSpeech tts;
    private boolean isReady = false;

    public TTSManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSManager", "Language not supported");
                } else {
                    isReady = true;
                }
            } else {
                Log.e("TTSManager", "Initialization failed");
            }
        });
    }

    public void doTTS(String text) {
        if (!isReady) {
            Log.w("TTSManager", "TTS not ready yet");
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            Log.w("TTSManager", "No text to speak");
            return;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID");
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
