package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;

import java.util.Arrays;

public class DemoRecognizer extends AbstractRecognizer {
  private final TencentVadRecorder recorder;

  public DemoRecognizer(TencentVadRecorder recorder) {
    this.recorder = recorder;
    recorder.registerRecordListener((asrData, length) -> {
      //asrData: pcm, 16000 sampleRate, 8bit
      //Receive the recording data of microphone output in line here
      byte[] quarter1 = new byte[512];
      byte[] quarter2 = new byte[512];
      byte[] quarter3 = new byte[512];
      byte[] quarter4 = new byte[512];
      loadArray(asrData, quarter1, 0);
      loadArray(asrData, quarter2, 512);
      loadArray(asrData, quarter3, 1024);
      loadArray(asrData, quarter4, 1536);
      Log.i(MainActivity.TAG, "Data: " + Arrays.toString(quarter1));
      Log.i(MainActivity.TAG, "Data: " + Arrays.toString(quarter2));
      Log.i(MainActivity.TAG, "Data: " + Arrays.toString(quarter3));
      Log.i(MainActivity.TAG, "Data: " + Arrays.toString(quarter4));
    }, null, null);
    recorder.registerStateListener((val, ex) -> {
      Log.i(MainActivity.TAG, "Recognizer state:\nValue: " + val);
      String err = ex == null ? "No error" : ex.toString();
      Log.i(MainActivity.TAG, "Ex: " + err);
    });
  }

  @Override protected void startRecognizing(RecognitionOption recognitionOption) {
    recorder.start();
    Log.i(MainActivity.TAG, "Recognizer: Recognized something");
  }

  @Override protected void stopRecognizing() {
    recorder.stop();
    Log.i(MainActivity.TAG, "Recognizer: Stop recognizing");
  }

  @Override
  protected void resolveRecognizing(RecognitionResult done) {
    super.resolveRecognizing(done);
    Log.i(MainActivity.TAG, "Recognizer: Resolve: " + done.getText());
  }
  private void loadArray(byte[] from, byte[] to, int fromIndex){
      for (int i = 0; i < 512; i++){
        to[i] = from[i + fromIndex];
      }
  }
}
