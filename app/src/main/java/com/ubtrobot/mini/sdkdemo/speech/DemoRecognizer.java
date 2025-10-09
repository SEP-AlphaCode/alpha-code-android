package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import com.ubtrobot.mini.sdkdemo.models.RobotRequestTypes;
import com.ubtrobot.mini.sdkdemo.socket.RobotMessageBuilder;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;
import com.ubtrobot.mini.sdkdemo.utils.LedHelper;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemoRecognizer extends AbstractRecognizer {
    private static final String TAG = "RECOGNIZING";
    private final TencentVadRecorder recorder;
    private static final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private static final long SILENCE_TIMEOUT_MS = 5000; // 5 seconds timeout
    private boolean isRecording = false;
    private CommandHandler commandHandler;
    private LedHelper ledHelper;
    public DemoRecognizer(TencentVadRecorder recorder) {
        this.recorder = recorder;
        this.timeoutHandler = new Handler(Looper.getMainLooper());
        ledHelper = new LedHelper();

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

        this.commandHandler = new CommandHandler();
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
        byte[] protobufMessage = new RobotMessageBuilder()
                .setType(RobotRequestTypes.PROCESS_SPEECH)
                .setAsrData(convertBytesToIntArray(fullRecording))
                .build();
        if(RobotSocketManager.isInitialized()){
            RobotSocketManager.getInstance().sendBinaryMessage(protobufMessage);
            ledHelper.notifyState(0);
        } else {
            Log.e(TAG, "Socket not initialized");
        }
    }
    public int[] convertBytesToIntArray(byte[] data) {
        if (data == null) {
            return new int[0];
        }

        int[] result = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i]; // This automatically converts byte to int
        }
        return result;
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
