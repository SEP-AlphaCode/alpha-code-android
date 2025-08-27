package com.ubtrobot.mini.sdkdemo.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.speech.custom.TTSManager;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;

import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {
    private Context context;
    private TTSManager ttsManager;

    public DemoSynthesizer(Context context) {
        Log.i(MainActivity.TAG, "Init Synthesizer");
        this.context = context;
        this.ttsManager = new TTSManager(context);
    }

    @Override
    protected void startSynthesizing(SynthesisOption synthesisOption) {
        String text = synthesisOption.getInputText();
        Log.i(MainActivity.TAG, "Synthesizer: Start: " + text);
        try {
            ttsManager.doTTS(text);
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "Synthesizer: Error: " + e);
        }
    }

    @Override
    protected void stopSynthesizing() {
        Log.i(MainActivity.TAG, "Synthesizer: Stop");
    }

    @Override
    public List<SpeakingVoice> getSpeakingVoiceList() {
        Log.i(MainActivity.TAG, "Synthesizer: Get voice list");
        return null;
    }
}
