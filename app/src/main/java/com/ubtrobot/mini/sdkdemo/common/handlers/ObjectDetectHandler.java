package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtech.utilcode.utils.FileUtils;
import com.ubtrobot.mini.sdkdemo.apis.ObjectDetectApi;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.models.RobotRequestTypes;
import com.ubtrobot.mini.sdkdemo.models.response.DetectClosestResponse;
import com.ubtrobot.mini.sdkdemo.models.response.Detection;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.sdkdemo.socket.RobotMessageBuilder;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectDetectHandler {
    public void handleDetect(File imageFile, String lang) {
        byte[] imageBytes = FileUtils.readFile2Bytes(imageFile);
        byte[] messageContent = new RobotMessageBuilder()
                .setType(RobotRequestTypes.DETECT_OBJECT)
                .addParameter("lang", lang)
                .setImageData(imageBytes)
                .build();
        RobotSocketManager.getInstance().sendBinaryMessage(messageContent);
    }
}