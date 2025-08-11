package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.ubtrobot.mini.sdkdemo.R;

import java.io.File;


/**
 * TakePicApi的测试方法
 */

public class TakePicApiActivity extends Activity {
    private static final String TAG = DemoApp.DEBUG_TAG;
    private TakePicApi takePicApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_pic_api_layout);
        initRobot();
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
     * @param view
     */
    public void takePicImmediately(View view) {
        takePicApi.takePicImmediately(new ResponseListener<String>() {
            @Override
            public void onResponseSuccess(String imagePath) {
                Log.i(TAG, "Ảnh lưu tại: " + imagePath);
                Toast.makeText(getApplicationContext(), "saving " + imagePath, Toast.LENGTH_LONG).show();

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
                    Toast.makeText(getApplicationContext(), "QR code: " + qrContent, Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, "Không đọc được QR code trong ảnh");
                }
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMsg) {
                Log.i(TAG, "Chụp ảnh thất bại, errorCode=" + errorCode + ", errorMsg=" + errorMsg);
            }
        });
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
