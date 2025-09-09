package com.ubtrobot.mini.sdkdemo.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.ubtech.utilcode.utils.Utils;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TTSManager {
    private TextToSpeech tts;
    private boolean isReady = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, TTSCallback> callbackMap = new ConcurrentHashMap<>();
    private static TTSManager instance;

    private TTSManager(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSManager", "Language not supported");
                } else {
                    isReady = true;
                    Log.i("TTSManager", "TTS is ready");
                }
            } else {
                Log.e("TTSManager", "Initialization failed");
            }
        });

        // Một listener duy nhất cho tất cả utterance
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSCallback cb = callbackMap.get(utteranceId);
                if (cb != null) mainHandler.post(cb::onStart);
                Log.i("TTSManager", "TTS Started: " + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                TTSCallback cb = callbackMap.remove(utteranceId);
                if (cb != null) mainHandler.post(cb::onDone);
                Log.i("TTSManager", "TTS Done: " + utteranceId);
            }

            @Override
            public void onError(String utteranceId) {
                TTSCallback cb = callbackMap.remove(utteranceId);
                if (cb != null) mainHandler.post(cb::onError);
                Log.e("TTSManager", "TTS Error: " + utteranceId);
            }
        });
    }
    public static TTSManager createInstance(Context context) {
        instance = new TTSManager(context);
        return instance;
    }

    /**
     * Singleton pattern to get the TTSManager instance with Context from Utils
     * @return
     */
    public static TTSManager getInstance() {
        if(instance == null) {
            instance = new TTSManager(Utils.getContext().getApplicationContext());
        }
        return instance;
    }

    /**
     * Singleton pattern to get the TTSManager instance with Context from Utils
     * @return
     */
    public static TTSManager getInstance() {
        if(instance == null) {
            instance = new TTSManager(Utils.getContext().getApplicationContext());
        }
        return instance;
    }

    public void doTTS(String text) {
        doTTS(text, null);
    }

    public void doTTS(String text, TTSCallback callback) {
        if (!isReady) {
            Log.w("TTSManager", "TTS not ready yet → will skip text: " + text);
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            Log.w("TTSManager", "No text to speak");
            return;
        }

        String utteranceId = UUID.randomUUID().toString();
        if (callback != null) {
            callbackMap.put(utteranceId, callback);
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
