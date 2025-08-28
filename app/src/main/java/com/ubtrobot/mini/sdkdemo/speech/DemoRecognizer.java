package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoRecognizer extends AbstractRecognizer {
    private static final String TAG = "RECOGNIZING";
    private final TencentVadRecorder recorder;
    private static final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final STTApi sttApi = ApiClient.getPythonInstance().create(STTApi.class);

    // Timeout related variables
    private final Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private static final long SILENCE_TIMEOUT_MS = 5000; // 5 seconds timeout
    private boolean isRecording = false;
    private ActionApiActivity actionApiActivity;
    private TakePicApiActivity takePicApiActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TTSManager tts;

    public DemoRecognizer(TencentVadRecorder recorder) {
        this.recorder = recorder;
        this.timeoutHandler = new Handler(Looper.getMainLooper());

        recorder.registerRecordListener((asrData, length) -> {
            //asrData: pcm, 16000 sampleRate, 8bit
            //Receive the recording data of microphone output in line here
            outputStream.write(asrData, 0, length);

            // Reset timeout when receiving audio data
            if (isRecording) {
                resetSilenceTimeout();
            }
        }, null, null);

        recorder.registerStateListener((val, ex) -> {
            if (val != TencentVadRecorder.STATE_SPEAK_END) {
                return;
            }
            Log.i(TAG, "Speech end");
            stopRecordingAndProcess();
        });

        initRobot();
    }

    private void initRobot() {
        actionApiActivity = ActionApiActivity.get();
        takePicApiActivity = TakePicApiActivity.get();
        tts = new TTSManager(Utils.getContext().getApplicationContext());
    }

    @Override
    protected void startRecognizing(RecognitionOption recognitionOption) {
        recorder.start();
        isRecording = true;
        startSilenceTimeout();
        Log.i(TAG, "Recognizer: Recognized something");
    }

    @Override
    protected void stopRecognizing() {
        isRecording = false;
        cancelSilenceTimeout();
        recorder.stop();
        try {
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "64:" + e);
        }
        Log.i(TAG, "Recognizer: Stop recognizing");
    }

    private void startSilenceTimeout() {
        timeoutRunnable = () -> {
            Log.i(TAG, "Silence timeout reached, stopping recording");
            if (isRecording) {
                stopRecordingAndProcess();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, SILENCE_TIMEOUT_MS);
    }

    private void resetSilenceTimeout() {
        cancelSilenceTimeout();
        startSilenceTimeout();
    }

    private void cancelSilenceTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    private void stopRecordingAndProcess() {
        isRecording = false;
        cancelSilenceTimeout();
        recorder.stop();

        byte[] fullRecording = getFullRecording();
        outputStream.reset();
        STTRequest request = new STTRequest(fullRecording);

        sttApi.doSTT(request).enqueue(new Callback<NLPResponse>() {
            @Override
            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        NLPResponse nlpResponse = response.body();
                        String type = nlpResponse.getType();
                        String text = nlpResponse.getData() != null ? nlpResponse.getData().getText() : "I didn't catch that. Could you please repeat?";
                        if (type != null) {
                            switch (type) {
                                case "qr-code":
                                    if (response.body().getData() != null) {
                                        tts.doTTS(text, new TTSManager.TTSCallback() {
                                            @Override
                                            public void onStart() {
                                                Log.i(TAG, "TTS started: " + text);
                                            }

                                            @Override
                                            public void onDone() {
                                                Log.i(TAG, "After voice played successfully");

                                                takePicApiActivity.takePicImmediately("qr-code");
                                            }

                                            @Override
                                            public void onError() {
                                                Log.e(TAG, "Error playing TTS: " + text);
                                            }
                                        });
                                    }
                                    break;
                                case "osmo-card":
                                    if (nlpResponse.getData() != null) {
                                        tts.doTTS(text, new TTSManager.TTSCallback() {
                                            @Override
                                            public void onStart() {
                                                Log.i(TAG, "TTS started: " + text);
                                            }

                                            @Override
                                            public void onDone() {
                                                Log.i(TAG, "After voice played successfully");

                                                actionApiActivity.playActionToTakeQR("takelowpic");

                                                handler.postDelayed(() -> {
                                                    takePicApiActivity.takePicImmediately("osmo-card");
                                                }, 3000); // Delay 3 seconds before taking picture
                                            }

                                            @Override
                                            public void onError() {
                                                Log.e(TAG, "Error playing TTS: " + text);
                                            }
                                        });
                                    }
                                    break;
                                default:
                                    if (text != null) {
                                        tts.doTTS(text);
                                    }
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                        recorder.start();
                    }
                }
            }

            @Override
            public void onFailure(Call<NLPResponse> call, Throwable t) {
                Log.e(TAG, "Response failure: " + t);
            }
        });
    }

    @Override
    protected void resolveRecognizing(RecognitionResult done) {
        super.resolveRecognizing(done);
        Log.i(TAG, "Recognizer: Resolve: " + done.getText());
    }

    private byte[] getFullRecording() {
        return outputStream.toByteArray();
    }
}
