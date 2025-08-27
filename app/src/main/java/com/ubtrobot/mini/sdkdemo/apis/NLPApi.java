package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.requests.NLPRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface NLPApi {
        @Streaming
        @POST("/nlp/tts")
        Call<ResponseBody> doTTS(@Body NLPRequest request);
}
