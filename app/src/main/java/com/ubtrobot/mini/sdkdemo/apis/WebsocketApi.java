package com.ubtrobot.mini.sdkdemo.apis;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WebsocketApi {
    @GET("/websocket/ws/disconnect/{serial}")
    Call<ResponseBody> disconnect(@Path("serial") String serial);
}
