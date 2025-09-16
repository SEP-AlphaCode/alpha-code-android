package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtrobot.mini.sdkdemo.apis.ObjectDetectApi;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.models.response.DetectClosestResponse;
import com.ubtrobot.mini.sdkdemo.models.response.Detection;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectDetectHandler {
    private static final String TAG = "ObjectDetectHandler";
    private final ObjectDetectApi api = ApiClient.getPythonInstance().create(ObjectDetectApi.class);
    private TTSHandler tts = new TTSHandler();
    private final STTApi stt = ApiClient.getPythonInstance().create(STTApi.class);

    public void handleDetect(File imageFile, String lang) {
        RequestBody reqFile = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), reqFile);

        // Example: call detect_closest
        Call<DetectClosestResponse> call = api.detectClosest(body);

        call.enqueue(new Callback<DetectClosestResponse>() {
            @Override
            public void onResponse(Call<DetectClosestResponse> call, Response<DetectClosestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DetectClosestResponse result = response.body();
                    Log.i(TAG, "Closest Objects: " + result.closest_objects.size());
                    for (int i = 0; i < result.closest_objects.size(); i++) {
                        Log.i(TAG, " - " + result.closest_objects.get(i).label +
                                " (depth_min=" + result.closest_objects.get(i).depth_min + ")");
                    }
                    if(!result.closest_objects.isEmpty()) {
                        Detection closest = result.closest_objects.get(0);
                        stt.doSTT(closest.label, lang).enqueue(new Callback<NLPResponse>() {
                            @Override
                            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                                NLPResponse r = response.body();
                                tts.doTTS(r.getData().getText(), lang);
                            }

                            @Override
                            public void onFailure(Call<NLPResponse> call, Throwable t) {

                            }
                        });
                    } else {
                        String text = "I don't see anything. Please try again";
                        if(lang.equals("vi")){
                            text = "Tôi không thấy gì cả. Xin hãy thử lại sau";
                        }
                        tts.doTTS(text, lang);
                    }
                } else {
                    Log.e(TAG, "Response failed: " + response.code());
                    LogManager.log(LogLevel.ERROR, TAG, "Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DetectClosestResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage(), t);
                LogManager.log(LogLevel.ERROR, TAG, "Request failed: " + t.getMessage());
            }
        });
    }
}