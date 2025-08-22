package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;

import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {
  public DemoSynthesizer(){
    Log.i(MainActivity.TAG, "Init Synthesizer");
  }
  @Override protected void startSynthesizing(SynthesisOption synthesisOption) {
    Log.i(MainActivity.TAG, "Synthesizer: Start: " + synthesisOption.getInputText());
    VoicePool vp = VoicePool.get();
    vp.playTTs(synthesisOption.getInputText(), Priority.HIGH, new VoiceListener() {
      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(int i, String s) {
        Log.e(MainActivity.TAG, s);
      }
    });
  }

  @Override protected void stopSynthesizing() {
    Log.i(MainActivity.TAG, "Synthesizer: Stop");
  }

  @Override public List<SpeakingVoice> getSpeakingVoiceList() {
    Log.i(MainActivity.TAG, "Synthesizer: Get voice list");
    return null;
  }
}
