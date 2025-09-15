package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.google.gson.Gson;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.apis.ObjectDetectApi;
import com.ubtrobot.mini.sdkdemo.custom.TTSManager;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.models.response.DetectClosestResponse;
import com.ubtrobot.mini.sdkdemo.models.response.Detection;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;
import com.ubtrobot.mini.voice.VoicePool;

import org.json.JSONException;
import org.json.JSONObject;

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
    private final STTApi sttApi = ApiClient.getPythonInstance().create(STTApi.class);

    public void handleDetect(File imageFile, String lang) {
        RequestBody reqFile = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), reqFile);

        // Example: call detect_closest
        Call<DetectClosestResponse> call = api.detectClosest(body, 1);

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
                    if (!result.closest_objects.isEmpty()) {
                        Detection closest = result.closest_objects.get(0);
                        sttApi.objectDetectResult(closest.label, lang).enqueue(new Callback<NLPResponse>() {
                            @Override
                            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                                NLPResponse r = response.body();
                                if(r == null) return;
                                NLPResponse.DataContainer data = r.getData();
                                // Convert JSON string -> JSONObject
                                try {
                                    String jsonString = new Gson().toJson(data);
                                    JSONObject jsonData = new JSONObject(jsonString);
                                    (new CommandHandler()).handleCommand(r.getType(), r.getLang(), jsonData);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onFailure(Call<NLPResponse> call, Throwable t) {

                            }
                        });
                    } else {
                        if(lang.equals("en")){
                            TTSHandler.doTTS("I didn't find any objects nearby.", lang, null);
                        } else {
                            TTSHandler.doTTS("Tôi không thấy vật gì", lang, null);
                        }
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