package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.ubtrobot.mini.sdkdemo.activity.SystemActivity;

public class SysEventTestActivity extends Activity {
    private static String TAG = "BatteryTest";
    private SystemActivity sys;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sys = SystemActivity.get();
        setContentView(R.layout.battery_test_activity);
        Button serial = findViewById(R.id.serial_button), firmware = findViewById(R.id.firmware_ver_button),
                ctrl = findViewById(R.id.ctrl_button), battery = findViewById(R.id.battery_button);
        EditText text = findViewById(R.id.result_text);

        serial.setOnClickListener(v -> {
            try {
                text.setText(sys.getSerialNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                text.setText("Error: " + e.getMessage());
            }
        });
        firmware.setOnClickListener(v -> {
            try {
                text.setText(sys.getFirmwareVersion());
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                text.setText("Error: " + e.getMessage());
            }
        });
        ctrl.setOnClickListener(v -> {
            try {
                text.setText(sys.getCtrlVersion());
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                text.setText("Error: " + e.getMessage());
            }
        });
        battery.setOnClickListener(v -> {
            try {
                text.setText(sys.getBatteryInfo());
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                text.setText("Error: " + e.getMessage());
            }
        });
    }
}
