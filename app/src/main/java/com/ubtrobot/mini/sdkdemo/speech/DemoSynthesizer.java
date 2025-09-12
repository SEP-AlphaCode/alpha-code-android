package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTS;
import com.ubtrobot.mini.sdkdemo.custom.tts.ViTTSManager;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;
import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {
    TTS tts = ViTTSManager.getInstance(Utils.getContext().getApplicationContext());
    public DemoSynthesizer() {
    }

    @Override
    protected void startSynthesizing(SynthesisOption synthesisOption) {
        tts.doTTS(synthesisOption.getInputText());
        resolveSynthesizing();
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
