package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.custom.tts.EnglishTTS;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTS;
import com.ubtrobot.mini.sdkdemo.custom.tts.VietnameseTTS;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;
import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {
    TTS viTts = VietnameseTTS.getInstance(Utils.getContext().getApplicationContext());
    TTS enTTS = EnglishTTS.getInstance(Utils.getContext().getApplicationContext());
    public DemoSynthesizer() {
    }

    @Override
    protected void startSynthesizing(SynthesisOption synthesisOption) {
        viTts.doTTS(synthesisOption.getInputText());
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
