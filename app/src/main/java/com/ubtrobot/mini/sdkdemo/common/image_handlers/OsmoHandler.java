package com.ubtrobot.mini.sdkdemo.common.image_handlers;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.apis.OsmoApi;
import com.ubtrobot.mini.sdkdemo.models.response.ActionResponseDto;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OsmoHandler {
    private static String TAG = "OsmoHandler";
    private OsmoApi osmoApi = ApiClient.getPythonInstance().create(OsmoApi.class);

    public void handleOsmo(File file) {
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                file
        );

        // Create MultipartBody.Part to send
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        // Call API
        osmoApi.recognizeActionCardFromImage(body).enqueue(new Callback<ActionResponseDto>() {
            @Override
            public void onResponse(Call<ActionResponseDto> call, Response<ActionResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Log.i(TAG, "Action cards: " + response.body().action_cards);
                        Log.i(TAG, "Actions: " + response.body().actions);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Data is null or response is not successful");
                }
            }

            @Override
            public void onFailure(Call<ActionResponseDto> call, Throwable t) {
                Log.e(TAG, "Error when call API: " + t.getMessage());
            }
        });
    }
}
