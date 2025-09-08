package com.ubtrobot.mini.sdkdemo.common.image_handlers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
import com.ubtrobot.mini.sdkdemo.activity.QrCodeActivity;
import com.ubtrobot.mini.sdkdemo.apis.ActivityApi;
import com.ubtrobot.mini.sdkdemo.apis.OsmoApi;
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

public class ImageHandler {
    private QrHandler qrHandler = new QrHandler();
    private OsmoHandler osmoHandler = new OsmoHandler();
    private ObjectDetectHandler objectDetectHandler = new ObjectDetectHandler();

    public void handleActions(String action, File file, String realPath) {
        switch (action) {
            case "osmo-card":
                osmoHandler.handleOsmo(file);
                break;
            case "qr-code":
                // Decode the QR code from the image file
                qrHandler.handleQr(realPath);
                break;
            case "object-detect":
                objectDetectHandler.handleDetect(file);
                break;
            default:
                Log.w("ImageHandler", "Unknown action: " + action);
        }
    }

}
