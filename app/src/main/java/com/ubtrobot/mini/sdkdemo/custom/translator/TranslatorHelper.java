package com.ubtrobot.mini.sdkdemo.custom.translator;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.nl.translate.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class TranslatorHelper {
    private static String TAG = "TranslatorHelper";
    private static TranslatorHelper instance;
    private final Translator translator;

    private TranslatorHelper(Context context) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build();
        translator = Translation.getClient(options);
    }

    public static synchronized void init(Context context) {
        try {
            if (instance == null) {
                instance = new TranslatorHelper(context);
                instance.translate("Hello", text -> {
                }, e -> {
                }); // Preload model
            }
            Log.i(TAG, "TranslatorHelper initialized");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TranslatorHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TranslatorHelper not initialized. Call init(context) first.");
        }
        return instance;
    }

    public void translate(String text, OnSuccessListener<String> success, OnFailureListener failure) {
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> translator.translate(text)
                        .addOnSuccessListener(success)
                        .addOnFailureListener(failure))
                .addOnFailureListener(failure);
    }

    public void close() {
        translator.close();
    }
}