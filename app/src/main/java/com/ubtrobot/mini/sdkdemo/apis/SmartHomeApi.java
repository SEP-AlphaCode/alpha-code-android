package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.response.VoiceResponseDto;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SmartHomeApi {
    @POST("esp32s/{id}/send-message")
    Call<VoiceResponseDto> smartHomeControl(
            @Path("id") String id,
            @Query("name") String name,
            @Query("message") String message,
            @Query("language") String language
    );
}
