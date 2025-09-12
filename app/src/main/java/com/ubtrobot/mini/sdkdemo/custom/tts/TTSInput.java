package com.ubtrobot.mini.sdkdemo.custom.tts;

public class TTSInput {
    private String lang;
    private String text;

    public String getLang() {
        return lang;
    }
    public String getText() {
        return text;
    }
    public TTSInput(String lang, String text) {
        this.lang = lang;
        this.text = text;
    }
}
