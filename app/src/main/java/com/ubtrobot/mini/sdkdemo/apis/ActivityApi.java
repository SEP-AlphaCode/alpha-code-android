package com.ubtrobot.mini.sdkdemo.apis;

import com.ubtrobot.mini.sdkdemo.models.response.QRCodeActivityResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ActivityApi {
    @GET("activities/get-by-qr-code/{qrCode}")
    Call<QRCodeActivityResponse> getQrCodeByCode(@Path("qrCode") String qrCode);
}
