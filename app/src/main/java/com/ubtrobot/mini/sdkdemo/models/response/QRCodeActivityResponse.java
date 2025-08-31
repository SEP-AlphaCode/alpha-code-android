package com.ubtrobot.mini.sdkdemo.models.response;

import com.google.gson.JsonObject;

import java.util.UUID;

public class QRCodeActivityResponse {
    private UUID id;
    private String name;
    private String type;
    private JsonObject data;

    public QRCodeActivityResponse(UUID id, String name, String type, JsonObject data) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public QRCodeActivityResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}