package com.ubtrobot.mini.sdkdemo.models.response;


import com.google.gson.JsonObject;

public class QRCodeDetectResponse {
    private int id;
    private String code;
    private String type;
    private JsonObject data;

    public QRCodeDetectResponse(int id, String code, String type, JsonObject data) {
        this.id = id;
        this.code  = code;
        this.type = type;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public JsonObject getData() {
        return data;
    }
}
