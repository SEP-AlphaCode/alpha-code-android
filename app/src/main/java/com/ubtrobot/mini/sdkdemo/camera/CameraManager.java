package com.ubtrobot.mini.sdkdemo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.*;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Collections;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private Camera camera;

    public void openCamera(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
        }
    }

    public void focusOnPoint(int x, int y, int width, int height) {
        if (camera == null) return;
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            Camera.Area area = new Camera.Area(
                    new android.graphics.Rect(
                            x - 50, y - 50, x + 50, y + 50
                    ), 1000
            );
            params.setFocusAreas(java.util.Collections.singletonList(area));
            camera.setParameters(params);
        }
        camera.autoFocus(null);
    }

    public void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public boolean isCameraAvailable() {
        return camera != null;
    }
}
