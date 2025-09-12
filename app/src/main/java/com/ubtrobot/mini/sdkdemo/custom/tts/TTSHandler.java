package com.ubtrobot.mini.sdkdemo.custom.tts;

import android.content.Context;
public class TTSHandler {
    private static TTS enTTS, vnTTS;
    public static void init(Context context){
        if(enTTS == null){
            enTTS = EnglishTTS.getInstance(context);
        }
        if(vnTTS == null){
            vnTTS = VietnameseTTS.getInstance(context);
        }
    }
    public static void doTTS(String text, String lang, TTSCallback callback){
        if(lang != null && lang.toLowerCase().startsWith("vi")){
            vnTTS.doTTS(text, callback);
        } else {
            enTTS.doTTS(text, callback);
        }
    }
    public static void doTTS(String text, String lang){
        if(lang != null && lang.toLowerCase().startsWith("vi")){
            vnTTS.doTTS(text, null);
        } else {
            enTTS.doTTS(text, null);
        }
    }
}
