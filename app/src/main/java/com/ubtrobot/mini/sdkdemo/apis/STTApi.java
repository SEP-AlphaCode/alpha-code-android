package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.requests.NLPRequest;
import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface STTApi {
    @POST("/stt/with-action")
    Call<NLPResponse> doSTT(@Body STTRequest request);
    @POST("/nlp/object-detect-result")
    Call<NLPResponse> objectDetectResult(@Query("label") String label, @Query("lang") String lang);
    @POST("/nlp/process-text")
    Call<NLPResponse> processText(@Body NLPRequest request);
}
