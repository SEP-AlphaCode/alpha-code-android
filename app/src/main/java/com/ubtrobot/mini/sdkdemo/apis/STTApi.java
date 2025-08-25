package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface STTApi {
    @POST("/stt/with-action")
    Call<NLPResponse> doSTT(@Body STTRequest request);
}
