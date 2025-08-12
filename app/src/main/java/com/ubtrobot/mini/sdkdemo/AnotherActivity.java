package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.ubtrobot.mini.sdkdemo.service.RobotWebSocketService;

public class AnotherActivity extends Activity {
    private TextView connectionStatus;

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            updateConnectionStatus(status);
        }
    };
    void goToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.another);

        connectionStatus = findViewById(R.id.connection_status);
        Button b = (Button) findViewById(R.id.go_to_main);
        b.setOnClickListener(l -> {
            goToMain();
        });
        // Start the WebSocket service
        startService(new Intent(this, RobotWebSocketService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver, new IntentFilter("WEBSOCKET_CONNECTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    private void updateConnectionStatus(String status) {
        runOnUiThread(() -> {
            switch (status) {
                case "connected":
                    connectionStatus.setText("Connected to server");
                    break;
                case "disconnected":
                    connectionStatus.setText("Disconnected - attempting to reconnect...");
                    break;
                case "error":
                    connectionStatus.setText("Connection error");
                    break;
            }
        });
    }
}