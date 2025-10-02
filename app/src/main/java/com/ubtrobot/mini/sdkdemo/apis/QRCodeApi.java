package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface QRCodeApi {
    @GET("activities/get-by-qr-code/{qrCode}")
    Call<QRCodeActivityResponse> getQrCodeByCode(@Path("qrCode") String qrCode);

    @Multipart
    @POST("qr-codes/by-image")
    Call<QRCodeActivityResponse> getQrCodeByImage(
            @Part MultipartBody.Part imageFile
    );
}
