package com.ubtrobot.mini.sdkdemo.speech;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisOption;
import java.util.List;

public class DemoSynthesizer extends AbstractSynthesizer {
  VoicePool vp;
  public  DemoSynthesizer(){
    vp = VoicePool.get();
  }
  @Override protected void startSynthesizing(SynthesisOption synthesisOption) {
    Log.i("SYN", "Play");
    vp.playTTs(synthesisOption.getInputText(), Priority.HIGH, new VoiceListener() {
      @Override
      public void onCompleted() {
        Log.i("SYN", "Complete");
      }

      @Override
      public void onError(int i, String s) {
        Log.i("SYN", "Error: " + s);
      }
    });
  }

  @Override protected void stopSynthesizing() {

  }

  @Override public List<SpeakingVoice> getSpeakingVoiceList() {
    return null;
  }
}
