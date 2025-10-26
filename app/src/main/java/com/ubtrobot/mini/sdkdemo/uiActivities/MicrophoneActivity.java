package com.ubtrobot.mini.sdkdemo.uiActivities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubtrobot.mini.sdkdemo.R;

public class MicrophoneActivity extends Activity {

    private static final int REQUEST_RECORD_AUDIO = 1;
    private static final int SAMPLE_RATE = 16000;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private Handler handler;

    private TextView statusText;
    private TextView infoText;
    private Button checkBtn;
    private Button recordBtn;
    private Button stopBtn;

    private int bufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone_check);

        handler = new Handler();
        setupViews();
        checkHardware();
    }

    private void setupViews() {
        statusText = findViewById(R.id.statusText);
        infoText = findViewById(R.id.infoText);
        checkBtn = findViewById(R.id.checkBtn);
        recordBtn = findViewById(R.id.recordBtn);
        stopBtn = findViewById(R.id.stopBtn);

        checkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkMicrophone();
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startRecording();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording();
            }
        });
    }

    private void checkHardware() {
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            setStatus("Hardware: Available");
        } else {
            setStatus("Hardware: Not available");
            disableButtons();
        }
    }

    private void checkMicrophone() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        } else {
            testMicrophone();
        }
    }

    private void testMicrophone() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                setStatus("Error: Invalid buffer size");
                return;
            }

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                setStatus("Working - Ready to record");
                recordBtn.setEnabled(true);
            } else {
                setStatus("Error: Failed to initialize");
            }

            if (audioRecord != null) {
                audioRecord.release();
            }

        } catch (SecurityException e) {
            setStatus("Error: Permission denied");
        } catch (Exception e) {
            setStatus("Error: " + e.getMessage());
        }
    }

    private void startRecording() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission needed", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                setInfo("Failed to start recording");
                return;
            }

            isRecording = true;
            audioRecord.startRecording();

            setInfo("Recording...");
            recordBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            startAudioMonitor();

        } catch (Exception e) {
            setInfo("Recording failed: " + e.getMessage());
        }
    }

    private void startAudioMonitor() {
        recordingThread = new Thread(new Runnable() {
            public void run() {
                short[] buffer = new short[bufferSize];

                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, bufferSize);
                    if (read > 0) {
                        // Calculate simple audio level
                        long sum = 0;
                        for (int i = 0; i < read; i++) {
                            sum += Math.abs(buffer[i]);
                        }
                        final double average = sum / (double) read;

                        handler.post(new Runnable() {
                            public void run() {
                                setInfo(String.format("Recording - Level: %.1f", average));
                            }
                        });
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });

        recordingThread.start();
    }

    private void stopRecording() {
        isRecording = false;

        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            } catch (Exception e) {
                // Ignore
            }
        }

        if (recordingThread != null) {
            try {
                recordingThread.join(500);
            } catch (InterruptedException e) {
                // Ignore
            }
            recordingThread = null;
        }

        setInfo("Recording stopped");
        recordBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    private void setStatus(final String text) {
        handler.post(new Runnable() {
            public void run() {
                statusText.setText("Status: " + text);
            }
        });
    }

    private void setInfo(final String text) {
        handler.post(new Runnable() {
            public void run() {
                infoText.setText(text);
            }
        });
    }

    private void disableButtons() {
        handler.post(new Runnable() {
            public void run() {
                recordBtn.setEnabled(false);
                stopBtn.setEnabled(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                testMicrophone();
            } else {
                setStatus("Permission denied");
                disableButtons();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}