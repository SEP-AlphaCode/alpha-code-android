package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.support.annotation.NonNull;

import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtrobot.commons.ResponseListener;

import java.util.List;

public class FaceHandler {
    private static final String TAG = "FaceHandler";
    private final FaceApi faceApi = FaceApi.get();
    private TTSHandler tts = new TTSHandler();
    public void handleDetect(String lang){
        faceApi.faceDetect(10000, new ResponseListener<List<FaceInfo>>() {
            @Override
            public void onResponseSuccess(List<FaceInfo> faceInfos) {

            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                
            }
        });
    }
}
