package com.ubtrobot.mini.sdkdemo.models.requests;

public class NLPRequest {
    private final String text;
    public NLPRequest(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
