package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.mini.sdkdemo.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.sdkdemo.utils.RobotUtils;
import com.ubtrobot.mini.voice.MiniMediaPlayer;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.mini.voice.protos.VoiceProto;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
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
    private MiniMediaPlayer miniPlayer;
    private ActionApiActivity actionApiActivity;
    private TakePicApiActivity takePicApiActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());


    // Initialize these only once in constructor
    private final MasterContext context;
    private final VoiceProto.Source source;

    public DemoRecognizer(TencentVadRecorder recorder, TakePicApiActivity takePicApiActivity, ActionApiActivity actionApiActivity) {
        this.recorder = recorder;
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        this.takePicApiActivity = takePicApiActivity;
        this.actionApiActivity = actionApiActivity;

        // Initialize context and source only once here
        this.context = RobotUtils.getMasterContext();
        this.source = RobotUtils.getVoiceProtoSource();

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

        sttApi.doSTT2(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Response not successful or empty");
                    recorder.start();
                    return;
                }

                // --- Read metadata from headers ---
                String type = response.headers().get("X-Type");
                String text = response.headers().get("X-Text");
                String fileName = response.headers().get("X-File-Name");
                String duration = response.headers().get("X-Duration");
                String voice = response.headers().get("X-Voice");
                String textLength = response.headers().get("X-Text-Length");

                File outFile = null;
                try {
                    // --- Save body to temp file ---
                    File cacheDir = Utils.getContext().getCacheDir();
                    outFile = new File(cacheDir, fileName != null ? fileName : "tts_" + System.currentTimeMillis() + ".wav");

                    BufferedInputStream in = new BufferedInputStream(response.body().byteStream());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

                    byte[] buffer = new byte[8 * 1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                    out.close();
                    in.close();

                    Log.i(TAG, "Saved TTS file at: " + outFile.getAbsolutePath());

                    // --- Play the file ---
                    miniPlayer = MiniMediaPlayer.create(context, source);
                    miniPlayer.setDataSource(outFile.getAbsolutePath());
                    miniPlayer.prepareAsync();

                    File finalOutFile = outFile; // capture for lambda
                    miniPlayer.setOnPreparedListener(mp -> {
                        Log.i(TAG, "Media ready, start playing");
                        mp.start();
                    });

                    miniPlayer.setOnCompletionListener(mp -> {
                        Log.i(TAG, "Playback completed, cleaning up");
                        recorder.start();
                        finalOutFile.delete();  // delete after play
                    });

                    miniPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e(TAG, "Error playing media: " + what + ", " + extra);
                        recorder.start();
                        finalOutFile.delete();
                        return true;
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Error saving/playing audio: " + e.getMessage(), e);
                    recorder.start();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "withAction2 failure: " + t);
                recorder.start();
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
