package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;
import com.ubtrobot.express.protos.Express;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import com.ubtrobot.mini.sdkdemo.R;


/**
 * ExpressApi的测试方法
 */

public class ExpressApiActivity extends Activity {
    private static final String TAG = DemoApp.DEBUG_TAG;
  private ExpressApi expressApi;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.express_api_layout);

    initRobot();
  }

  /**
   * 初始化接口类实例
   */
  private void initRobot() {
    expressApi = ExpressApi.get();
  }

  /**
   * 获取表情列表
   */
  public void getExpressList(View view) {
      List<Express.ExpressInfo> expressList = expressApi.getExpressList();

      // Tạo nội dung text từ expressList
      StringBuilder sb = new StringBuilder();
      for (Express.ExpressInfo expressInfo : expressList) {
          sb.append(expressInfo.toString()).append("\n");
      }

      // File lưu vào thư mục Download
      File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      if (!downloadsDir.exists()) {
          downloadsDir.mkdirs();
      }
      File outFile = new File(downloadsDir, "express_list.txt");

      try (FileOutputStream fos = new FileOutputStream(outFile);
           OutputStreamWriter writer = new OutputStreamWriter(fos)) {
          writer.write(sb.toString());
          writer.flush();
          Log.i(TAG, "File saved to " + outFile.getAbsolutePath());
      } catch (IOException e) {
          Log.e(TAG, "Error saving file: " + e.getMessage(), e);
      }
  }

  /**
   * 获取自定义表情列表, 自定义表情资源存放在/sdcard/customize/expresss目录
   *
   * 系统版本需v0.0.3以上
   */
  public void getCustomizeExpressList(View view) {
    List<Express.ExpressInfo> expressList = expressApi.getCustomizeExpressList();
    for (Express.ExpressInfo expressInfo : expressList) {
      Log.i(TAG, expressInfo.toString());
    }
    Log.i(TAG, "getExpressList接口调用成功！");
  }

  /**
   * 执行表情
   */
  public void doExpress(View view) {
    expressApi.doExpress("wakeup", 2, Priority.HIGH, new AnimationListener() {
      @Override public void onAnimationStart() {
        Log.i(TAG, "doExpress开始执行表情!");
      }

      @Override public void onAnimationEnd(int i) {
        Log.i(TAG, "doExpress表情执行结束!");
      }

      @Override public void onAnimationRepeat(int loopNumber) {
        Log.i(TAG, "doExpress重复执行表情,重复次数:" + loopNumber);
      }
    });
    Log.i(TAG, "doExpress接口调用成功!");
  }

  /**
   * 执行自定义表情
   */
  public void doCustomizeExpress(View view) {
      List<Express.ExpressInfo> expressList = expressApi.getCustomizeExpressList();
      if(expressList.size() == 0){
          Toast.makeText(getApplicationContext(),"no custom express found !",Toast.LENGTH_LONG).show();
      }else{
          expressApi.doCustomizeExpress(expressList.get(0).getName(), Priority.HIGH, new AnimationListener() {
              @Override public void onAnimationStart() {
                  Log.i(TAG, "doCustomizeExpress开始执行表情!");
              }

              @Override public void onAnimationEnd(int i) {
                  Log.i(TAG, "doCustomizeExpress表情执行结束!");
              }

              @Override public void onAnimationRepeat(int loopNumber) {
                  Log.i(TAG, "doCustomizeExpress重复执行表情,重复次数:" + loopNumber);
              }
          });
      }

    Log.i(TAG, "doCustomizeExpress接口调用成功!");
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
