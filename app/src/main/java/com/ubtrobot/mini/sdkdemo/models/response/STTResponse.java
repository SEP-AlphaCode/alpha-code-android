package com.ubtrobot.mini.sdkdemo.models.response;

import com.ubtrobot.mini.sdkdemo.models.requests.STTRequest;

public class STTResponse {
    private final String text;

    public String getText() {
        return text;
    }
    public STTResponse(String text){
        this.text = text;
    }
}
