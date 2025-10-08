//package com.ubtrobot.mini.sdkdemo.custom.translate;
//
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.mlkit.nl.translate.TranslateLanguage;
//import com.google.mlkit.nl.translate.Translation;
//import com.google.mlkit.nl.translate.Translator;
//import com.google.mlkit.nl.translate.TranslatorOptions;
//
//public class LocalTranslator {
//    TranslatorOptions options =
//            new TranslatorOptions.Builder()
//                    .setSourceLanguage(TranslateLanguage.ENGLISH)
//                    .setTargetLanguage(TranslateLanguage.VIETNAMESE)
//                    .build();
//    final Translator translator =
//            Translation.getClient(options);
//    boolean isModelDownloaded = false;
//    private static final String TAG = "LocalTranslator";
//    public static LocalTranslator INSTANCE;
//    private LocalTranslator() {
//        translator.downloadModelIfNeeded().addOnSuccessListener(unused -> isModelDownloaded = true);
//    }
//    public static void init(){
//        INSTANCE = new LocalTranslator();
//    }
//    public static void translate(String text, OnSuccessListener<String> onSuccessListener) {
//        if (INSTANCE.isModelDownloaded) {
//            INSTANCE.translator.translate(text)
//                    .addOnSuccessListener(onSuccessListener);
//        } else {
//            INSTANCE.translator.downloadModelIfNeeded().addOnSuccessListener(unused -> {
//                INSTANCE.isModelDownloaded = true;
//                INSTANCE.translator.translate(text)
//                        .addOnSuccessListener(onSuccessListener);
//            });
//        }
//    }
//}
