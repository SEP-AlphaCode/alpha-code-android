package com.ubtrobot.mini.sdkdemo.socket;

import java.util.HashMap;
import java.util.Map;

public class RobotMessageBuilder {
    private String type;
    private int[] asrData;
    private byte[] imageData;
    private Map<String, String> parameters;
    private String speechText;

    public RobotMessageBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public RobotMessageBuilder setAsrData(int[] asrData) {
        this.asrData = asrData;
        return this;
    }

    public RobotMessageBuilder setImageData(byte[] imageData) {
        this.imageData = imageData;
        return this;
    }

    public RobotMessageBuilder setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RobotMessageBuilder addParameter(String key, String value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
        return this;
    }

    public RobotMessageBuilder setSpeechText(String speechText) {
        this.speechText = speechText;
        return this;
    }

    public byte[] build() {
        if (type == null) {
            throw new IllegalStateException("Type is required");
        }

        return ProtobufConverter.requestToProtoBytes(
                type, asrData, imageData, parameters, speechText
        );
    }
}