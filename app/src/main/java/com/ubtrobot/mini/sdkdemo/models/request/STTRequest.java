package com.ubtrobot.mini.sdkdemo.models.request;

public class STTRequest {
    private byte[] arr;
    public STTRequest(byte[] arr){
        this.arr = arr;
    }

    public byte[] getArr() {
        return arr;
    }

    public void setArr(byte[] arr) {
        this.arr = arr;
    }
}
