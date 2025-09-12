package com.ubtrobot.mini.sdkdemo.custom.tts;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnTTSManager implements TTS {
    private TextToSpeech tts;
    private boolean isReady = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, TTSCallback> callbackMap = new ConcurrentHashMap<>();
    private static EnTTSManager instance;
    private Context context;

    private EnTTSManager(Context context) {
        this.context = context.getApplicationContext();
        init(context);
    }

    public static synchronized EnTTSManager getInstance(Context context) {
        if (instance == null) {
            instance = new EnTTSManager(context);
        }
        return instance;
    }

    public static synchronized EnTTSManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EnTTSManager must be initialized first with getInstance(Context)");
        }
        return instance;
    }

    @Override
    public void init(Context context) {
        if (tts != null) {
            shutdown();
        }

        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("EnTTSManager", "Language not supported");
                    isReady = false;
                } else {
                    isReady = true;
                    Log.i("EnTTSManager", "TTS is ready");
                }
            } else {
                Log.e("EnTTSManager", "Initialization failed");
                isReady = false;
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSCallback cb = callbackMap.get(utteranceId);
                if (cb != null) mainHandler.post(cb::onStart);
                Log.i("EnTTSManager", "TTS Started: " + utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                TTSCallback cb = callbackMap.remove(utteranceId);
                if (cb != null) mainHandler.post(cb::onDone);
                Log.i("EnTTSManager", "TTS Done: " + utteranceId);
            }

            @Override
            public void onError(String utteranceId) {
                TTSCallback cb = callbackMap.remove(utteranceId);
                if (cb != null) mainHandler.post(cb::onError);
                Log.e("EnTTSManager", "TTS Error: " + utteranceId);
            }
        });
    }

    @Override
    public void doTTS(String text) {
        doTTS(text, null);
    }

    @Override
    public void doTTS(String text, TTSCallback callback) {
        if (!isReady) {
            Log.w("EnTTSManager", "TTS not ready yet → will skip text: " + text);
            if (callback != null) {
                mainHandler.post(callback::onError);
            }
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            Log.w("EnTTSManager", "No text to speak");
            if (callback != null) {
                mainHandler.post(callback::onError);
            }
            return;
        }

        String utteranceId = UUID.randomUUID().toString();
        if (callback != null) {
            callbackMap.put(utteranceId, callback);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    @Override
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        callbackMap.clear();
        isReady = false;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }
}