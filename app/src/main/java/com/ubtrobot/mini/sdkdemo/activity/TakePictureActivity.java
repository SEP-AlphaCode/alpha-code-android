package com.ubtrobot.mini.sdkdemo.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.sauron.api.TakePicApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.apis.ActivityApi;
import com.ubtrobot.mini.sdkdemo.apis.OsmoApi;
import com.ubtrobot.mini.sdkdemo.common.image_handlers.ImageHandler;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.models.response.ActionResponseDto;
import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TakePictureActivity {
    private static final String TAG = "TakePictureActivity";
    private TakePicApi takePicApi;

    ImageHandler imageHandler = new ImageHandler();
    private TTSManager tts;


    public static TakePictureActivity get() {
        return TakePictureActivity.Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static TakePictureActivity _api = new TakePictureActivity();
    }

    private void initRobot() {
        takePicApi = TakePicApi.get();
        tts = TTSManager.getInstance();
    }

    public void takePicImmediately(String action) {
        if(takePicApi == null){
            initRobot();
        }
        if (takePicApi != null) {
            takePicApi.takePicImmediately(new ResponseListener<String>() {
                @Override
                public void onResponseSuccess(String imagePath) {
                    Log.i(TAG, "Save image at: " + imagePath);

                    // Check if context is available before showing toast
                    try {
                        if (Utils.getContext().getApplicationContext() != null) {
                            Toast.makeText(Utils.getContext().getApplicationContext(), "saving " + imagePath, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Cannot show toast, context not available: " + e.getMessage());
                    }

                    // Change the path from "/ubtrobot/camera/xxx" to "/sdcard/ubtrobot/camera/xxx"
                    String realPath = imagePath.replaceFirst("^/ubtrobot", "/sdcard/ubtrobot");

                    File file = new File(realPath);
                    if (file.exists()) {
                        Log.i(TAG, "File exists: " + realPath);
                    } else {
                        Log.e(TAG, "File not exists: " + realPath);
                    }

                    imageHandler.handleActions(action, file, realPath);
                }

                @Override
                public void onFailure(int errorCode, @NonNull String errorMsg) {
                    Log.i(TAG, "Take picture failed, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
                }
            });
        } else {
            Log.e(TAG, "TakePicApi is still null after initialization attempt");
        }
    }

    private String decodeQRCodeFromFile(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if (bitmap == null) {
            Log.e(TAG, "Can not load file from path: " + filePath);
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();

        try {
            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            Log.e(TAG, "Qr code not found in the image: " + e.getMessage());
            tts.doTTS("Qr code not found in the image, please try again.");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error when decode QR Code: " + e.getMessage());
            return null;
        }
    }
}
