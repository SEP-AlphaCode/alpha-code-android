package com.ubtrobot.mini.sdkdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;

import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceFindListener;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtechinc.sauron.api.FaceTrackListener;
import com.ubtechinc.sauron.api.SauronApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.voice.VoicePool;

import java.util.Arrays;
import java.util.List;

public class FaceApiActivity extends Activity {
    private static final String TAG = "FaceActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 199901;
    private static final int MAX_RETRY_ATTEMPTS = 1; // Retry only once
    private int retryCount = 0;
    private boolean shouldRetry = true;
    private FaceApi faceApi;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_api_layout);
        textureView = findViewById(R.id.camera_feed);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            setupCamera();
        }
        initRobot();
    }

    /**
     * Initialize the interface class instance
     */
    private void initRobot() {
        faceApi = FaceApi.get();
    }

    /**
     * Check if the robot is currently in the input process
     */
    public void checkFaceInsertState(View view) {
        faceApi.checkFaceInsertState(new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG, "checkFaceInsertState interface call successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "checkFaceInsertState,errorCode:" + errorCode + ",errorMsg:" + errorMsg);
            }
        });
    }

    /**
     * Face analysis: detects the face in the current image and analyzes it to obtain information such as age and gender. Note: does not include information about whether the face is familiar.
     */
    public void faceAnalyze(View view) {
        faceApi.faceAnalyze(15, new ResponseListener<List<FaceInfo>>() {
            @Override
            public void onResponseSuccess(List<FaceInfo> faceInfos) {
                for (FaceInfo faceInfo : faceInfos) {
                    Log.i(TAG, faceInfo.toString());
                }
                Log.i(TAG, "faceAnalyze interface call successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "faceAnalyze interface returns an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
    }

    /**
     * Instant offline face detection, detects the current face, and returns immediately upon detection.
     */
    public void faceDetect(View view) {
        faceApi.faceDetect(15, new ResponseListener<List<FaceInfo>>() {
            @Override
            public void onResponseSuccess(List<FaceInfo> faceInfos) {
                Log.i(TAG, "face detected size: " + faceInfos.size());
                for (FaceInfo faceInfo : faceInfos) {
                    Log.i(TAG, faceInfo.toString());
                }
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "The faceDetect interface returned an error, errorCode:" + errorCode + ", errorMsg:" + errorMsg);
            }
        });
    }

    /**
     * Face recognition, detecting facial information in the current image (whether it's a familiar face, not including age, gender, etc.)
     */
    public void faceRecognize(View view) {
        faceApi.faceRecognize(30, new ResponseListener<List<FaceInfo>>() {
            @Override
            public void onResponseSuccess(List<FaceInfo> faceInfos) {
                for (FaceInfo faceInfo : faceInfos) {
                    Log.i(TAG, faceInfo.toString());
                }
                Log.i(TAG, "The faceRecognize interface call was successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "The faceRecognize interface returned an error, errorCode:" + errorCode + ",errorMsg:" + errorMsg);
            }
        });
    }

    public void faceRegister(View view) {
        faceApi.startRegister("user_li", "MinhDuck", new ResponseListener<String>() {

            @Override
            public void onResponseSuccess(String msg) {
                Log.i(TAG, "faceRegisterStart call successful, msg======" + msg);
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "faceRegisterStart interface returns an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
        Log.i(TAG, "faceRegisterStart interface call successful!");
    }

    /**
     * Face registration skill startup interface
     */
    public void faceRegisterStart(View view) {
        faceApi.apiFaceRegister("user_li", "MinhDuck", new ResponseListener<String>() {

            @Override
            public void onResponseSuccess(String msg) {
                Log.i(TAG, "faceRegisterStart call successful, msg======" + msg);
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "faceRegisterStart interface returns an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
        Log.i(TAG, "faceRegisterStart interface call successful!");
    }

    /**
     * Exit the face registration process
     */
    public void faceRegisterStop(View view) {
        faceApi.stopRegister("user_li", new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG, "faceRegisterStop call successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "faceRegisterStop interface returned an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
        Log.i(TAG, "faceRegisterStop interface call successful!");
    }

    /**
     * Face tracking and recognition interface
     */
    public void faceTrack(View view) {
        Log.i(TAG, "faceTrack interface call successful!");
        faceApi.apiFaceTrack(15, true, new FaceTrackListener() {
            @Override
            public void onStart() {
                Log.i(TAG, "faceTrack starts tracking faces!");
            }

            @Override
            public void onFaceChange(List<FaceInfo> faceInfos) {
                for (FaceInfo faceInfo : faceInfos) {
                    Log.i(TAG, faceInfo.toString());
                }
            }

            @Override
            public void onStop() {
                Log.i(TAG, "faceTrack stops tracking faces!");
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                Log.i(TAG, "faceTrack interface returns an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
    }

    /**
     * Exit face tracking recognition
     */
    public void stopFaceTrack(View view) {
        SauronApi.get().stopAll(new ResponseListener<Boolean>() {
            @Override
            public void onResponseSuccess(Boolean aVoid) {
                Log.i(TAG, "stopFaceTrack call successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "stopFaceTrack interface returned an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
    }

    /**
     * Find faces. After turning on, the head servo will continuously rotate to find faces. During the process, a callback will be called to find the face list.
     */
    @Deprecated
    public void findFace(View view) {
        faceApi.findFace(20, new FaceFindListener() {
            @Override
            public void onPause() {
                Log.i(TAG, "findFace paused face search!");
            }

            @Override
            public void onStart() {
                Log.i(TAG, "findFace started searching for faces!");
            }

            @Override
            public void onFaceChange(List<FaceInfo> faceInfos) {
                Log.i(TAG, "findFace found a face!");
                for (FaceInfo faceInfo : faceInfos) {
                    Log.i(TAG, faceInfo.toString());
                }
            }

            @Override
            public void onStop() {
                Log.i(TAG, "findFace stopped searching for faces!");
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                Log.i(TAG, "findFace API returned an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
    }

    /**
     * Pauses searching for faces, pauses the servos, and does not turn off the camera. Call findFace() again to continue face search.
     */
    @Deprecated
    public void pauseFindFace(View view) {
        faceApi.pauseFindFace(new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG, "pauseFindFace call successful!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "pauseFindFace API returned an error, errorCode: " + errorCode + ", errorMsg: " + errorMsg);
            }
        });
    }

    /**
     * Exit face search, turn off the camera, and stop the servos.
     */
    @Deprecated
    public void stopFindFace(View view) {
        faceApi.stopFindFace(new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG, "stopFindFace was called successfully!");
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "stopFindFace interface returns error,errorCode:" + errorCode + ",errorMsg:" + errorMsg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length == 0) {
                // No camera available, just leave black screen
                return;
            }

            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
            });

        } catch (CameraAccessException e) {
            // Just leave black screen on initial setup error
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    retryCount = 0; // Reset retry count on success
                    createCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                    cameraDevice = null;
                    tryReopenCamera();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    if(cameraDevice != null){
                        cameraDevice.close();
                        cameraDevice = null;
                    }
                    tryReopenCamera();
                }
            }, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot open camera", e);
            tryReopenCamera();
        } 
    }
    private void tryReopenCamera() {
        if (shouldRetry && retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++;
            Log.d(TAG, "Retrying camera open, attempt: " + retryCount);

            // Wait a bit before retrying
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (shouldRetry) {
                    openCamera();
                }
            }, 1000); // Retry after 1 second

        } else {
            // Max retries reached or should not retry, just keep black screen
            Log.d(TAG, "Giving up on camera after " + retryCount + " attempts");
            shouldRetry = false; // Stop any further retries
            // TextureView will remain black (default state)
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) return;

            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);

            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;

                            cameraCaptureSession = session;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                        null, null);
                            } catch (CameraAccessException e) {
                                // Preview setup failed, try to reopen
                                tryReopenCamera();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            // Session configuration failed, try to reopen
                            tryReopenCamera();
                        }
                    }, null);

        } catch (CameraAccessException e) {
            tryReopenCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            }
            // If denied, TextureView will remain black (no error message)
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldRetry = false; // Stop any ongoing retries

        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldRetry = true;
        retryCount = 0;

        if (textureView.isAvailable()) {
            openCamera();
        }
    }
}