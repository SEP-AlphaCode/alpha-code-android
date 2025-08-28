package com.ubtrobot.mini.sdkdemo.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.ubtrobot.mini.voice.VoicePool;

import java.util.Locale;

public class TTSManager {
    private TextToSpeech tts;
    private boolean isReady = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // interface callback để bên ngoài truyền vào
    public interface TTSCallback {
        void onStart();
        void onDone();
        void onError();
    }

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

        // gắn listener mặc định
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.i("TTSManager", "TTS Started: " + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                Log.i("TTSManager", "TTS Done: " + utteranceId);
            }

            @Override
            public void onError(String utteranceId) {
                Log.e("TTSManager", "TTS Error: " + utteranceId);
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

    // thêm overload có callback
    public void doTTS(String text, TTSCallback callback) {
        if (!isReady) {
            Log.w("TTSManager", "TTS not ready yet");
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            Log.w("TTSManager", "No text to speak");
            return;
        }

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                mainHandler.post(callback::onStart);
            }

            @Override
            public void onDone(String utteranceId) {
                mainHandler.post(callback::onDone);
            }

            @Override
            public void onError(String utteranceId) {
                mainHandler.post(callback::onError);
            }
        });

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID");
    }



    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
