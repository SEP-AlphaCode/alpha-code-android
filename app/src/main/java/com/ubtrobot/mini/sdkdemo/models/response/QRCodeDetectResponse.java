package com.ubtrobot.mini.sdkdemo.models.response;


import com.google.gson.JsonObject;

public class QRCodeDetectResponse {
    private int id;
    private String name;
    private JsonObject data;

    public QRCodeDetectResponse(int id, String name, JsonObject data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JsonObject getData() {
        return data;
    }
}
