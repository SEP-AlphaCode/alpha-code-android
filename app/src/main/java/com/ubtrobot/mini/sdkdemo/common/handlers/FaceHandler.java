package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.support.annotation.NonNull;

import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.custom.tts.TTSCallback;

import java.util.List;
import java.util.UUID;

public class FaceHandler {
    private static final String TAG = "FaceHandler";
    private final FaceApi faceApi = FaceApi.get();
    private TTSHandler tts = new TTSHandler();
    private static FaceHandler instance;

    public static FaceHandler get(){
        if(instance == null){
            instance = new FaceHandler();
        }
        return instance;
    }
    public void handleDetect(String lang) {
        tts.doTTS(lang.equals("en") ? "Ok, let me see you" : "Được rồi, để tôi nhìn bạn nào", lang, new TTSCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onDone() {
                faceApi.faceDetect(10000, new ResponseListener<List<FaceInfo>>() {
                    @Override
                    public void onResponseSuccess(List<FaceInfo> faceInfos) {

                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {

                    }
                });
            }

            @Override
            public void onError() {

            }
        });
    }
    public void handleRegister(String name){
        faceApi.apiFaceRegister(UUID.randomUUID().toString(), name, new ResponseListener<String>() {
            @Override
            public void onResponseSuccess(String s) {

            }

            @Override
            public void onFailure(int i, @NonNull String s) {

            }
        });
    }
}
