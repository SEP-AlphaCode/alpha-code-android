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
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrHandler {
    private static final String TAG = "QrHandler";
    private QrCodeActivity qrCodeActivity = QrCodeActivity.get();
    private ActivityApi activityApi = ApiClient.getSpringInstance().create(ActivityApi.class);
    private TTSManager tts = new TTSManager(Utils.getContext().getApplicationContext());
    public void handleQr(String realPath) {
        String qrContent = decodeQRCodeFromFile(realPath);
        if (qrContent != null) {
            Log.i(TAG, "Qr Code content: " + qrContent);
            // Call api to get QR code details
            activityApi.getQrCodeByCode(qrContent).enqueue(new Callback<QRCodeActivityResponse>() {
                @Override
                public void onResponse(Call<QRCodeActivityResponse> call, Response<QRCodeActivityResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JsonObject jsonObject = response.body().getData();

                            // Change JsonObject (Gson) -> JSON string
                            String jsonString = new Gson().toJson(jsonObject);

                            // Put string JSON to DoActivity
                            qrCodeActivity.DoActivity(jsonString, response.body().getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "Data: " + response.body().getData());
                    } else {
                        Log.e(TAG, "Data is null or response is not successful");
                    }
                }

                @Override
                public void onFailure(Call<QRCodeActivityResponse> call, Throwable t) {
                    Log.e(TAG, "Error when call API: " + t.getMessage());
                }
            });
        } else {
            Log.i(TAG, "Cannot decode QR code, file does not exist: " + realPath);
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
