package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.activity.TakePictureActivity;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;

public class CameraHandler {
    private static final String TAG = "CameraHandler";
    private TakePictureActivity takePictureActivity;
    private ActionApi actionApi;
    private VoicePool vp = VoicePool.get();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CameraHandler() {
        this.takePictureActivity = TakePictureActivity.get();
        this.actionApi = ActionApi.get();
    }

    public void handleQRCode(String text) {
        // Use default message if text is null or empty
        final String message = (text == null || text.trim().isEmpty())
                ? "Please show the QR code in front of me to take a picture. Now I will take the picture."
                : text;

        // Call TTS
        vp.playTTs(message, Priority.HIGH, new VoiceListener() {

            @Override
            public void onCompleted() {
                Log.i(TAG, "Voice playback finished successfully");
                // Only take picture if a QR code text was provided
                if (message != null && !message.trim().isEmpty()) {
                    takePictureActivity.takePicImmediately("qr-code");
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }

    public void handleOsmoCard(String text) {
        final String message = (text == null || text.trim().isEmpty())
                ? "Please place the OSMO card under my feet in my view. Now I will bend down to scan it."
                : text;

        vp.playTTs(message, Priority.HIGH, new VoiceListener() {

            @Override
            public void onCompleted() {
                Log.i(TAG, "After voice played successfully");
                actionApi.playCustomizeAction("takelowpic", null);

                handler.postDelayed(() -> {
                    takePictureActivity.takePicImmediately("osmo-card");
                }, 3000); // Delay 3 seconds before taking picture
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "Error playing TTS: " + message);
            }
        });
    }
}
