package com.ubtrobot.mini.sdkdemo.models.requests;

public class STTRequest {
    private final byte[] arr;
    public STTRequest(byte[] arr){
        this.arr = arr;
    }

    public byte[] getArr() {
        return arr;
    }
}
