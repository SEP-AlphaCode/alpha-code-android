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
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.apis.QRCodeApi;
import com.ubtrobot.mini.sdkdemo.models.QRCodeDetectResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.voice.VoicePool;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * TakePicApi的测试方法
 */

public class TakePicApiActivity extends Activity {
    private static final String TAG = DemoApp.DEBUG_TAG;
    private TakePicApi takePicApi;
    private QrCodeActivity qrCodeActivity;
    QRCodeApi qrCodeApi = ApiClient.getInstance().create(QRCodeApi.class);


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
    public void takePicImmediately() {
        // Ensure takePicApi is initialized
        if (takePicApi == null) {
            initRobot();
        }

        if (takePicApi != null) {
            takePicApi.takePicImmediately(new ResponseListener<String>() {
                @Override
                public void onResponseSuccess(String imagePath) {
                    Log.i(TAG, "Ảnh lưu tại: " + imagePath);

                    // Check if context is available before showing toast
                    try {
                        if (getApplicationContext() != null) {
                            Toast.makeText(getApplicationContext(), "saving " + imagePath, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Cannot show toast, context not available: " + e.getMessage());
                    }

                    // Chuyển đổi đường dẫn từ "/ubtrobot/camera/xxx" sang "/sdcard/ubtrobot/camera/xxx"
                    String realPath = imagePath.replaceFirst("^/ubtrobot", "/sdcard/ubtrobot");

                    File file = new File(realPath);
                    if (file.exists()) {
                        Log.i(TAG, "File tồn tại");
                    } else {
                        Log.e(TAG, "File không tồn tại: " + realPath);
                    }

                    // Giải mã QR code từ ảnh, dùng realPath
                    String qrContent = decodeQRCodeFromFile(realPath);
                    if (qrContent != null) {
                        Log.i(TAG, "Nội dung QR code: " + qrContent);
                        // Gọi API để lấy danh mục theo ID từ QR code
                        qrCodeApi.getQrCodeByCode(qrContent).enqueue(new Callback<QRCodeDetectResponse>() {
                            @Override
                            public void onResponse(Call<QRCodeDetectResponse> call, Response<QRCodeDetectResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    // Xử lý phản hồi thành công
                                    try {
                                        JsonObject jsonObject = response.body().getData();

                                        // Chuyển JsonObject (Gson) -> JSON string
                                        String jsonString = new Gson().toJson(jsonObject);

                                        // Truyền string JSON vào DoActivity
                                        qrCodeActivity.DoActivity(jsonString);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Log.i(TAG, "Danh mục: " + response.body().getData());
                                } else {
                                    Log.e(TAG, "Không tìm thấy danh mục hoặc phản hồi không hợp lệ");
                                }
                            }

                            @Override
                            public void onFailure(Call<QRCodeDetectResponse> call, Throwable t) {
                                Log.e(TAG, "Lỗi khi gọi API: " + t.getMessage());
                            }
                        });
                    } else {
                        Log.i(TAG, "Không đọc được QR code trong ảnh");
                    }
                }

                @Override
                public void onFailure(int errorCode, @NonNull String errorMsg) {
                    Log.i(TAG, "Chụp ảnh thất bại, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
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
            Log.e(TAG, "Không thể load ảnh từ file: " + filePath);
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
            Log.e(TAG, "QR code không tìm thấy trong ảnh");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi giải mã QR code: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
