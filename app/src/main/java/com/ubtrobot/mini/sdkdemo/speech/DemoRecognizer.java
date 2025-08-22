package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.models.response.STTResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoRecognizer extends AbstractRecognizer {
  private static final String TAG = "RECOGNIZING";
  private final TencentVadRecorder recorder;
  private static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private STTApi sttApi = ApiClient.getPythonInstance().create(STTApi.class);
  public DemoRecognizer(TencentVadRecorder recorder) {
    this.recorder = recorder;
    recorder.registerRecordListener((asrData, length) -> {
      //asrData: pcm, 16000 sampleRate, 8bit
      //Receive the recording data of microphone output in line here
      outputStream.write(asrData, 0, length);
    }, null, null);
    recorder.registerStateListener((val, ex) -> {
      if(val != TencentVadRecorder.STATE_SPEAK_END){
        return;
      }
      Log.i(TAG, "Speech end");
      byte[] fullRecording = getFullRecording();
      outputStream.reset();
      sttApi.doSTT(new STTRequest(fullRecording)).enqueue(new Callback<NLPResponse>() {
        @Override
        public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
          if (response.isSuccessful() && response.body() != null) {
            try {
              Log.i(TAG, "Type: " + response.body().getType());
              Log.i(TAG, "Data: " + response.body().getData().getText());

            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            Log.e(TAG, "Response data is null or response is not successful");
          }
        }

        @Override
        public void onFailure(Call<NLPResponse> call, Throwable t) {
          Log.e(TAG, "48: " + t);
        }
      });
    });
  }

  @Override protected void startRecognizing(RecognitionOption recognitionOption) {
    recorder.start();
    Log.i(TAG, "Recognizer: Recognized something");
  }

  @Override protected void stopRecognizing() {
    recorder.stop();
      try {
          outputStream.close();
      } catch (IOException e) {
          Log.e(TAG, "64:" + e);
      }
      Log.i(TAG, "Recognizer: Stop recognizing");
  }

  @Override
  protected void resolveRecognizing(RecognitionResult done) {
    super.resolveRecognizing(done);
    Log.i(TAG, "Recognizer: Resolve: " + done.getText());
  }
  private void loadArray(byte[] from, byte[] to, int fromIndex){
      for (int i = 0; i < 512; i++){
        to[i] = from[i + fromIndex];
      }
  }
  private byte[] getFullRecording() {
    return outputStream.toByteArray();
  }
}
