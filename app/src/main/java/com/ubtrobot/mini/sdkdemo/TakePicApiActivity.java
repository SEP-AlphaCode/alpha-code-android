package com.ubtrobot.mini.sdkdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
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
import com.ubtrobot.mini.sdkdemo.activity.QrCodeActivity;
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


/**
 * TakePicApi的测试方法
 */

public class TakePicApiActivity extends Activity {
    private static final String TAG = "TakePicApiActivity";
    private TakePicApi takePicApi;
    ImageHandler imageHandler = new ImageHandler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_pic_api_layout);
        initRobot();
    }

    public static TakePicApiActivity get() {
        return TakePicApiActivity.Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static TakePicApiActivity _api = new TakePicApiActivity();
    }

    /**
     * 初始化接口类实例
     */
    private void initRobot() {
        takePicApi = TakePicApi.get();
    }

    /**
     * 拍照(立即)
     *
     *
     */
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
                        if (getApplicationContext() != null) {
                            Toast.makeText(getApplicationContext(), "saving " + imagePath, Toast.LENGTH_LONG).show();
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


    /**
     * 拍照(寻找人脸)
     *
     * @param view
     */
    public void takePicWithFaceDetect(View view) {
        takePicApi.takePicWithFaceDetect(new ResponseListener<String>() {
            @Override
            public void onResponseSuccess(String string) {
                Log.i(TAG, "takePicWithFaceDetect接口调用成功！");
                Toast.makeText(getApplicationContext(), "saving " + string, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "takePicWithFaceDetect接口调用失败,errorCode======" + errorCode + ",errorMsg======" + errorMsg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
