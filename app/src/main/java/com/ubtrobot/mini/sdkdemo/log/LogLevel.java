package com.ubtrobot.mini.sdkdemo.log;

public enum LogLevel {
    INFO("INFO"),
    ERROR("ERROR"),
    WARN("WARN"),
    DEBUG("DEBUG");
    private final String value;

    LogLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
