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
import com.ubtech.utilcode.utils.FileUtils;
import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.sauron.api.TakePicApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.apis.QRCodeApi;
import com.ubtrobot.mini.sdkdemo.apis.OsmoApi;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.models.RobotRequestTypes;
import com.ubtrobot.mini.sdkdemo.models.response.ActionResponseDto;
import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.sdkdemo.socket.RobotMessageBuilder;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;

import org.json.JSONObject;

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

    public static TakePictureActivity get() {
        return TakePictureActivity.Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static TakePictureActivity _api = new TakePictureActivity();
    }

    private void initRobot() {
        takePicApi = TakePicApi.get();
    }

    public void takePicImmediately(String action, String lang) {
        if (takePicApi == null) {
            initRobot();
        }
        if (takePicApi != null) {
            takePicApi.takePicImmediately(new ResponseListener<String>() {
                @Override
                public void onResponseSuccess(String imagePath) {
                    // Check if context is available before showing toast
                    try {
                        if (Utils.getContext().getApplicationContext() != null) {
                            Toast.makeText(Utils.getContext().getApplicationContext(), "saving " + imagePath, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Cannot show toast, context not available: " + e.getMessage());
                        LogManager.log(LogLevel.WARN, TAG, "Cannot show toast, context not available: " + e.getMessage());
                    }

                    // Change the path from "/ubtrobot/camera/xxx" to "/sdcard/ubtrobot/camera/xxx"
                    String realPath = imagePath.replaceFirst("^/ubtrobot", "/sdcard/ubtrobot");

                    File file = new File(realPath);
                    if (file.exists()) {
                        Log.i(TAG, "File exists: " + realPath);
                    } else {
                        Log.e(TAG, "File not exists: " + realPath);
                        LogManager.log(LogLevel.ERROR, TAG, "File not exists: " + realPath);
                    }

                    switch (action) {
                        case "osmo-card":
                            byte[] messageContent = new RobotMessageBuilder()
                                    .setType(RobotRequestTypes.PARSE_OSMO)
                                    .setImageData(FileUtils.readFile2Bytes(file))
                                    .build();
                            RobotSocketManager.getInstance().sendBinaryMessage(messageContent);
                            break;
                        case "qr-code":
                            messageContent = new RobotMessageBuilder()
                                    .setType(RobotRequestTypes.PARSE_QR)
                                    .setImageData(FileUtils.readFile2Bytes(file))
                                    .build();
                            RobotSocketManager.getInstance().sendBinaryMessage(messageContent);
                            break;
                        case "video-capture":
                            messageContent = new RobotMessageBuilder()
                                    .setType(RobotRequestTypes.PARSE_VIDEO_GENERATE)
                                    .setImageData(FileUtils.readFile2Bytes(file))
                                    .build();
                            RobotSocketManager.getInstance().sendBinaryMessage(messageContent);
                            break;
                    }
                }

                @Override
                public void onFailure(int errorCode, @NonNull String errorMsg) {
                    Log.i(TAG, "Take picture failed, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
                    LogManager.log(LogLevel.ERROR, TAG, "Take picture failed, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
                }
            });
        } else {
            Log.e(TAG, "TakePicApi is still null after initialization attempt");
            LogManager.log(LogLevel.ERROR, TAG, "TakePicApi is still null after initialization attempt");
        }
    }
}
