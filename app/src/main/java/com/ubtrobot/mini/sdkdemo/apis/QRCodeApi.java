package com.ubtrobot.mini.sdkdemo.apis;
import com.ubtrobot.mini.sdkdemo.models.QRCodeDetectResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface QRCodeApi {
    @GET("qr-codes/by-code/{code}")
    Call<QRCodeDetectResponse> getQrCodeByCode(@Path("code") String code);
}
