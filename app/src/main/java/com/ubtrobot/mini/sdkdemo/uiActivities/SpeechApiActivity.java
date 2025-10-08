package com.ubtrobot.mini.sdkdemo.uiActivities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.camera.CameraManager;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import com.ubtrobot.mini.sdkdemo.models.requests.NLPRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeechApiActivity extends Activity {
    private static final String TAG = "SpeechApiActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private EditText textContent;
    private Button sendBtn, qrBtn, osmoBtn;

    private CommandHandler cmd;
    private STTApi api;

    private CameraManager cameraManager;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_test_activity);

        // --- UI ---
        textContent = findViewById(R.id.input_text);
        sendBtn = findViewById(R.id.send_btn);
        qrBtn = findViewById(R.id.qr_btn);
        osmoBtn = findViewById(R.id.osmo_btn);

        sendBtn.setOnClickListener(this::sendText);
        qrBtn.setOnClickListener(v -> textContent.setText("Scan the qr code"));
        osmoBtn.setOnClickListener(v -> textContent.setText("Scan the osmo cards"));

        // --- Command handler & API ---
        cmd = new CommandHandler();
        api = ApiClient.getPythonInstance().create(STTApi.class);

        // --- Camera setup ---
        surfaceView = findViewById(R.id.camera_surface);
        surfaceHolder = surfaceView.getHolder();
        cameraManager = new CameraManager();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                checkCameraPermissionAndOpen(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraManager.closeCamera();
            }
        });

        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (cameraManager.isCameraAvailable()) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    cameraManager.focusOnPoint(x, y, surfaceView.getWidth(), surfaceView.getHeight());
                }
                return true;
            }
            return false;
        });
    }

    // --- Camera permission check ---
    private void checkCameraPermissionAndOpen(SurfaceHolder holder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            cameraManager.openCamera(holder);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(surfaceHolder);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- Send text to NLP API ---
    public void sendText(View view) {
        String inputText = textContent.getText().toString().trim();
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show();
            return;
        }

        sendBtn.setEnabled(false); // disable button to avoid spam

        api.processText(new NLPRequest(inputText)).enqueue(new Callback<NLPResponse>() {
            @Override
            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                sendBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        NLPResponse nlpResponse = response.body();
                        String type = nlpResponse.getType();
                        NLPResponse.DataContainer data = nlpResponse.getData();

                        String jsonString = new Gson().toJson(data);

                        // Convert JSON string -> org.json.JSONObject
                        JSONObject jsonData = new JSONObject(jsonString);

                        // Handle command
                        cmd.handleCommand(type, jsonData, nlpResponse.getLang());

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing NLP response", e);
                        Toast.makeText(SpeechApiActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API response not successful");
                    Toast.makeText(SpeechApiActivity.this, "API response failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NLPResponse> call, Throwable t) {
                sendBtn.setEnabled(true);
                Log.e(TAG, "API call failed", t);
                Toast.makeText(SpeechApiActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.closeCamera();
    }
}
