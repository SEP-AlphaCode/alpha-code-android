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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
                DetectClosestResponse result = response.body();
                if(result.closest_objects.isEmpty()){
                    if(lang.equals("en")){
                        tts.doTTS("I don't see anything. Please try again", lang);
                    } else {
                        tts.doTTS("Tôi không thấy gì cả. Vui lòng thử lại", lang);
                    }
                    return;
                }
                String objectName = result.closest_objects.get(0).label;
                stt.describeObjectDetectResult(objectName, lang).enqueue(new Callback<NLPResponse>() {
                    @Override
                    public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                        NLPResponse nlpResponse = response.body();
                        String text = nlpResponse.getData().getText();
                        tts.doTTS(text, lang);
                    }

                    @Override
                    public void onFailure(Call<NLPResponse> call, Throwable t) {
                        Log.e(TAG, "NLP request failed: " + t.getMessage(), t);
                        LogManager.log(LogLevel.ERROR, TAG, "NLP request failed: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<DetectClosestResponse> call, Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage(), t);
                LogManager.log(LogLevel.ERROR, TAG, "Request failed: " + t.getMessage());
            }
        });
    }

    private String buildDetectionSentence(List<Detection> detections) {
        if (detections == null || detections.isEmpty()) {
            return "I don't see anything. Please try again";
        }
        LinkedHashSet<String> uniqueLabels = new LinkedHashSet<>();
        for (Detection d : detections) {
            uniqueLabels.add(d.label);
        }
        List<String> labels = new ArrayList<>(uniqueLabels);
        if (labels.size() == 1) {
            return "I see a " + labels.get(0) + ".";
        }
        StringBuilder sb = new StringBuilder("I see ");
        for (int i = 0; i < 1; i++) {
            sb.append("a ").append(labels.get(i));
            if (i < labels.size() - 2) {
                sb.append(", ");
            } else if (i == labels.size() - 2) {
                sb.append(", and ");
            }
        }
        sb.append(".");
        return sb.toString();
    }
}