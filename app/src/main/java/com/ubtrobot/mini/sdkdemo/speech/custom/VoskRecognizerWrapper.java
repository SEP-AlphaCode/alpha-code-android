package com.ubtrobot.mini.sdkdemo.speech.custom;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.parcelable.BaseProgress;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.RecognitionException;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionProgress;
import com.ubtrobot.speech.RecognitionResult;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VoskRecognizerWrapper extends AbstractRecognizer {
    private static final String TAG = "VOSK_RECOGNIZER";
    private final Context appContext;
    private final String modelPath;      // directory containing Vosk model files
    private Model model;                  // cached after first load
    private Recognizer voskRecognizer;    // per-session
    private SpeechService speechService;  // per-session

    public VoskRecognizerWrapper(Context context, String modelPath) {
        this.appContext = context.getApplicationContext();
        this.modelPath = modelPath;
    }

    @Override
    protected void startRecognizing(RecognitionOption option) {
        Log.i(TAG, "Vosk: Start recognizing with option: " + option);
        new Thread(() -> {
            try {
                // Lazily load model once
                if (model == null) {
                    model = new Model(modelPath);
                }
                // Create recognizer and speech service for this session
                voskRecognizer = new Recognizer(model, 16000.0f); // 16k mono PCM
                speechService  = new SpeechService(voskRecognizer, 16000.0f);

                // Notify "began"
                reportRecognizingProgress(
                        new RecognitionProgress.Builder(BaseProgress.PROGRESS_BEGAN)
                                .setDecibel(0)
                                .build()
                );

                Log.i(TAG, "Vosk: Recognizer and SpeechService created, starting listening...");
                speechService.startListening(new org.vosk.android.RecognitionListener() {
                    @Override
                    public void onPartialResult(String hypothesis) {
                        String text = extractText(hypothesis);
                        if (!text.isEmpty()) {
                            reportRecognizingProgress(
                                    new RecognitionProgress.Builder(RecognitionProgress.PROGRESS_RECOGNITION_TEXT_RESULT)
                                            .setTextResult(text)
                                            .build()
                            );
                        }
                    }

                    @Override
                    public void onResult(String hypothesis) {
                        // Intermediate final chunk (Vosk streams multiple finals sometimes)
                        String text = extractText(hypothesis);
                        Log.i(TAG, "Vosk: Intermediate final result: " + text);
                        resolveRecognizing(new RecognitionResult.Builder(text).build());
                    }

                    @Override
                    public void onFinalResult(String hypothesis) {
                        String text = extractText(hypothesis);
                        Log.i(TAG, "Vosk: Final result: " + text);
                        resolveRecognizing(new RecognitionResult.Builder(text).build());
                        reportRecognizingProgress(
                                new RecognitionProgress.Builder(BaseProgress.PROGRESS_ENDED).build()
                        );
                        cleanupSession();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.i(TAG, "Vosk: Error: " + e);
                        rejectRecognizing(new RecognitionException(-50, e.getMessage(), e));
                        cleanupSession();
                    }

                    @Override
                    public void onTimeout() {
                        // Treat timeout as an ended session (no extra error)
                        reportRecognizingProgress(
                                new RecognitionProgress.Builder(BaseProgress.PROGRESS_ENDED).build()
                        );
                        cleanupSession();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Vosk: Failed to start recognizing: " + e, e);
                rejectRecognizing(new RecognitionException(-50, "Vosk init/start failed", e));
                cleanupSession();
            }
        }).start();
    }

    @Override
    protected void stopRecognizing() {
        Log.i(TAG, "Vosk: Stop recognizing called");
        // Called by your interruptible helper on cancel
        cleanupSession();
        // Signal ended if caller expects an ENDED progress
        reportRecognizingProgress(
                new RecognitionProgress.Builder(BaseProgress.PROGRESS_ENDED).build()
        );
    }

    private void cleanupSession() {
        Log.i(TAG, "Vosk: Session cleaned up");
        if (speechService != null) {
            try { speechService.stop(); } catch (Throwable ignored) {}
            try { speechService.shutdown(); } catch (Throwable ignored) {}
            speechService = null;
        }
        voskRecognizer = null; // keep 'model' cached for next session
    }

    /** Vosk JSON: partial => {"partial":"..."} ; final => {"text":"..."} */
    private static String extractText(String json) {
        try {
            JSONObject o = new JSONObject(json);
            if (o.has("text"))    return o.optString("text", "");
            if (o.has("partial")) return o.optString("partial", "");
        } catch (Exception ignored) {}
        return "";
    }

    public static String copyAssets(Context context, String assetDir) throws IOException {
        File outDir = new File(context.getFilesDir(), assetDir);
        if (!outDir.exists()) {
            outDir.mkdirs();
            AssetManager assetManager = context.getAssets();
            String[] files = assetManager.list(assetDir);
            for (String filename : files) {
                String fullAssetPath = assetDir + "/" + filename;
                String[] subFiles = assetManager.list(fullAssetPath);

                if (subFiles != null && subFiles.length > 0) {
                    // it's a directory → recursive copy
                    copyAssets(context, fullAssetPath);
                } else {
                    // it's a file → copy
                    InputStream in = assetManager.open(fullAssetPath);
                    File outFile = new File(outDir, filename);
                    OutputStream out = new FileOutputStream(outFile);

                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.close();
                }
            }
        }
        return outDir.getAbsolutePath();
    }

}