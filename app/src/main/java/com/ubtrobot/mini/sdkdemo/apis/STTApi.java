package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.request.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.STTResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface STTApi {
    @POST("/stt")
    Call<STTResponse> doSTT(@Body STTRequest request);
}
