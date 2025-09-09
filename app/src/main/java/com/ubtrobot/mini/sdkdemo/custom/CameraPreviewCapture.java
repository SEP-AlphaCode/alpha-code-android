package com.ubtrobot.mini.sdkdemo.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.ubtrobot.mini.sdkdemo.common.handlers.ObjectDetectHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraPreviewCapture {
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private ObjectDetectHandler objectDetectHandler;
    private Context context;
    private boolean captured = false;

    public CameraPreviewCapture(Context context) {
        this.context = context;
        this.objectDetectHandler = new ObjectDetectHandler();
    }

    public void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    public void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {
        startBackgroundThread();
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0]; // back camera

            // Prepare ImageReader to grab ONE preview frame
            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                if (captured) return; // already processed one frame
                captured = true;

                Image image = reader.acquireLatestImage();
                if (image != null) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    image.close();

                    // 🔹 Send to detection handler
                    File tempFile = saveTempImage(bytes);
                    objectDetectHandler.handleDetect(tempFile);

                    // stop everything after capture
                    closeCamera();
                }
            }, backgroundHandler);

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                }
            }, backgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            Surface surface = imageReader.getSurface();
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);

            cameraDevice.createCaptureSession(
                    Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                    },
                    backgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
            stopBackgroundThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File saveTempImage(byte[] data) {
        try {
            File file = File.createTempFile("capture", ".jpg", context.getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
