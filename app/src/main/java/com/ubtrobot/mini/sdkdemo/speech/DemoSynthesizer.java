package com.ubtrobot.mini.sdkdemo.speech;

import android.content.Context;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;

import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {

    public DemoSynthesizer() {
    }

    @Override
    protected void startSynthesizing(SynthesisOption synthesisOption) {
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
