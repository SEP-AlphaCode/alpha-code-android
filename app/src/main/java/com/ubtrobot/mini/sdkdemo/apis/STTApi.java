package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface STTApi {
    @POST("/stt/with-action")
    Call<NLPResponse> doSTT(@Body STTRequest request);
    @Streaming
    @POST("/stt/with-action-wav")
    Call<ResponseBody> doSTT2(@Body STTRequest request);
}
