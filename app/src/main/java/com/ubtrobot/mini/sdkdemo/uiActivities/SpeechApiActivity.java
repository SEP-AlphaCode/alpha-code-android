package com.ubtrobot.mini.sdkdemo.uiActivities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
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
    private static String TAG = "SpeechApiActivity";
    EditText textContent;
    Button sendBtn, qr, osmo;
    CommandHandler cmd = new CommandHandler();
    STTApi api = ApiClient.getPythonInstance().create(STTApi.class);

    private CameraManager cameraManager;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_test_activity);
        textContent = findViewById(R.id.input_text);
        sendBtn = findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(this::sendText);
        qr = findViewById(R.id.qr_btn);
        qr.setOnClickListener(v -> {
            textContent.setText("Scan the qr code");
        });
        osmo = findViewById(R.id.osmo_btn);
        osmo.setOnClickListener(v -> {
            ;
            textContent.setText("Scan the osmo cards");
        });

        surfaceView = findViewById(R.id.camera_surface);
        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                int width = surfaceView.getWidth();
                int height = surfaceView.getHeight();
                cameraManager.focusOnPoint(x, y, width, height);
                return true;
            }
            return false;
        });
        surfaceHolder = surfaceView.getHolder();
        cameraManager = new CameraManager();
        cameraManager.openCamera(surfaceHolder);
    }

    public void sendText(View view) {
        api.processText(new NLPRequest(textContent.getText().toString())).enqueue(new Callback<NLPResponse>() {
            @Override
            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        NLPResponse nlpResponse = response.body();
                        String type = nlpResponse.getType();
                        NLPResponse.DataContainer data = nlpResponse.getData();
                        // Convert DataContainer -> JSON string
                        String jsonString = new Gson().toJson(data);

                        // Convert JSON string -> JSONObject
                        JSONObject jsonData = new JSONObject(jsonString);
                        // Use CommandHandler instead of switch case
                        cmd.handleCommand(type, jsonData, nlpResponse.getLang());

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response in speech api activity", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<NLPResponse> call, Throwable t) {
                Log.e(TAG, "Error processing response in speech api activity", t);
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        cameraManager.closeCamera();
    }
}
