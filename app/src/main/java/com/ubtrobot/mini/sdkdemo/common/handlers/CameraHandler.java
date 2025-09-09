package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.mini.sdkdemo.activity.TakePictureActivity;
import com.ubtrobot.mini.sdkdemo.custom.TTSCallback;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;

public class CameraHandler {
    private static final String TAG = "CameraHandler";
    private TakePictureActivity takePictureActivity;
    private ActionApi actionApi;
    private TTSManager tts;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CameraHandler() {
        this.takePictureActivity = TakePictureActivity.get();
        this.actionApi = ActionApi.get();
        this.tts = TTSManager.getInstance();
    }

    public void handleQRCode(String text) {
        // Use default message if text is null or empty
        final String message = (text == null || text.trim().isEmpty())
                ? "Please show the QR code in front of me to take a picture. Now I will take the picture."
                : text;

        // Call TTS
        tts.doTTS(message, new TTSCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "TTS started: " + message);
            }

            @Override
            public void onDone() {
                Log.i(TAG, "Voice playback finished successfully");
                // Only take picture if a QR code text was provided
                if (message != null && !message.trim().isEmpty()) {
                    takePictureActivity.takePicImmediately("qr-code");
                }
            }

            @Override
            public void onError() {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }

    public void handleOsmoCard(String text) {
        final String message = (text == null || text.trim().isEmpty())
                ? "Please place the OSMO card under my feet in my view. Now I will bend down to scan it."
                : text;

        tts.doTTS(message, new TTSCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "TTS started: " + message);
            }

            @Override
            public void onDone() {
                Log.i(TAG, "After voice played successfully");
                actionApi.playCustomizeAction("takelowpic", null);

                handler.postDelayed(() -> {
                    takePictureActivity.takePicImmediately("osmo-card");
                }, 3000); // Delay 3 seconds before taking picture
            }

            @Override
            public void onError() {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }
}
