package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.response.DetectClosestResponse;
import com.ubtrobot.mini.sdkdemo.models.response.DetectionResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ObjectDetectApi {
    @Multipart
    @POST("/object/detect")
    Call<DetectionResponse> detect(@Part MultipartBody.Part file);

    @Multipart
    @POST("/object/detect_closest")
    Call<DetectClosestResponse> detectClosest(@Part MultipartBody.Part file);
}
