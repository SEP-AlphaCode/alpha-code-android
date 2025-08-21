package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.models.request.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.STTResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;

import java.io.ByteArrayOutputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoRecognizer extends AbstractRecognizer {
    private final TencentVadRecorder recorder;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    STTApi api = ApiClient.getPythonInstance().create(STTApi.class);
    private static final String TAG = "RECOGNIZING";
    public DemoRecognizer(TencentVadRecorder recorder) {
        this.recorder = recorder;
        recorder.registerRecordListener((asrData, length) -> {
            //asrData: pcm, 16000 sampleRate, 8bit
            //Receive the recording data of microphone output in line here
            outputStream.write(asrData, 0, length);
        }, null, null);
        recorder.registerStateListener((val, ex) -> {
            if (val != TencentVadRecorder.STATE_SPEAK_END) {
                return;
            }
            Log.i(TAG, "Speech end");
            byte[] result = getFullRecording();
            outputStream.reset();
            long c1 = System.currentTimeMillis();
            try {
                api.doSTT(new STTRequest(result)).enqueue(new Callback<STTResponse>() {
                    @Override
                    public void onResponse(Call<STTResponse> call, Response<STTResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String text = response.body().getText();
                                Log.i(TAG, "Text: " + text);
                                VoicePool.get().playTTs("You said: " + text, Priority.HIGH, new VoiceListener() {
                                    @Override
                                    public void onCompleted() {
                                        Log.i(TAG, "Complete");
                                    }

                                    @Override
                                    public void onError(int i, String s) {
                                        Log.i(TAG, "Error: " + s);
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "55: error parsing text: " + e);
                            }

                        } else {
                            Log.e(TAG, "59: Response error");
                        }
                    }

                    @Override
                    public void onFailure(Call<STTResponse> call, Throwable t) {
                        Log.e(TAG, "67: API error: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.i("RECOGNIZING", "STTVosk Error: " + e);
            }
            long c2 = System.currentTimeMillis();
            Log.i(TAG, "Delta time: " + (c2 - c1) + " ms");
        });
    }

    @Override
    protected void startRecognizing(RecognitionOption recognitionOption) {
        outputStream.reset();
        recorder.start();
    }

    @Override
    protected void stopRecognizing() {
        recorder.stop();
    }

    @Override
    protected void resolveRecognizing(RecognitionResult done) {
        super.resolveRecognizing(done);
        Log.i(TAG, "resolve: " + done.getText());
    }

    private byte[] getFullRecording() {
        return outputStream.toByteArray();   // all chunks joined
    }
}
