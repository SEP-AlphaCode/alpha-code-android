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
import com.ubtechinc.sauron.api.TakePicApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.activity.QrCodeActivity;
import com.ubtrobot.mini.sdkdemo.apis.ActivityApi;
import com.ubtrobot.mini.sdkdemo.apis.OsmoApi;
import com.ubtrobot.mini.sdkdemo.models.response.ActionResponseDto;
import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.voice.VoicePool;

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
    private QrCodeActivity qrCodeActivity;
    private VoicePool vp = VoicePool.get();
    ActivityApi activityApi = ApiClient.getSpringInstance().create(ActivityApi.class);
    OsmoApi osmoApi = ApiClient.getPythonInstance().create(OsmoApi.class);


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
        qrCodeActivity = QrCodeActivity.get();
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

                    switch (action){
                        case "osmo-card":
                            RequestBody requestFile = RequestBody.create(
                                    MediaType.parse("image/jpeg"),
                                    file
                            );

                            // Create MultipartBody.Part to send
                            MultipartBody.Part body =
                                    MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                            // Call API
                            osmoApi.recognizeActionCardFromImage(body).enqueue(new Callback<ActionResponseDto>() {
                                @Override
                                public void onResponse(Call<ActionResponseDto> call, Response<ActionResponseDto> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        try {
                                            Log.i(TAG, "Action cards: " + response.body().action_cards);
                                            Log.i(TAG, "Actions: " + response.body().actions);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.e(TAG, "Data is null or response is not successful");
                                    }
                                }

                                @Override
                                public void onFailure(Call<ActionResponseDto> call, Throwable t) {
                                    Log.e(TAG, "Error when call API: " + t.getMessage());
                                }
                            });
                            break;
                        case "qr-code":
                            // Decode the QR code from the image file
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
                            break;
                    }
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
            vp.playTTs("Qr code not found in the image, please try again.", Priority.HIGH, null);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error when decode QR Code: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
