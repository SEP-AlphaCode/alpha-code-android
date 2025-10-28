package com.ubtrobot.mini.sdkdemo.uiActivities;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.models.RobotRequestTypes;
import com.ubtrobot.mini.sdkdemo.socket.RobotMessageBuilder;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;

public class SpeechApiActivity extends Activity {
    private static final String TAG = "SpeechApiActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private EditText textContent;
    private Button sendBtn, qrBtn, osmoBtn;

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
    }

    // --- Send text to NLP API ---
    public void sendText(View view) {
        String inputText = textContent.getText().toString().trim();
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show();
            return;
        }

        sendBtn.setEnabled(false); // disable button to avoid spam

        byte[] data = new RobotMessageBuilder().setType(RobotRequestTypes.PROCESS_TEXT)
                .addParameter("text", inputText)
                .build();
        RobotSocketManager.getInstance().sendBinaryMessage(data);
    }
}
