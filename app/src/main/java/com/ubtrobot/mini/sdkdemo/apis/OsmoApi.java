package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.response.ActionResponseDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface OsmoApi {
    @POST("osmo/recognize_action_cards_from_image")
    @Multipart
    Call<ActionResponseDto> recognizeActionCardFromImage(@Part MultipartBody.Part image);
}
