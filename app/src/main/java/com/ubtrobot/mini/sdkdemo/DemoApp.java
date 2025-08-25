package com.ubtrobot.mini.sdkdemo;

import android.app.Application;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.msc.util.log.DebugLog;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtrobot.master.log.InfrequentLoggerFactory;
import com.ubtrobot.mini.SDKInit;
import com.ubtrobot.mini.properties.sdk.Path;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.sdkdemo.speech.DemoMasterService;
import com.ubtrobot.mini.sdkdemo.speech.DemoSpeechJava;
import com.ubtrobot.service.ServiceModules;
import com.ubtrobot.speech.SpeechService;
import com.ubtrobot.speech.SpeechSettings;
import com.ubtrobot.ulog.FwLoggerFactory2;
import com.ubtrobot.ulog.logger.android.AndroidLoggerFactory;


public class DemoApp extends Application {

    public static final String DEBUG_TAG = "API_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
        PropertiesApi.setRootPath(Path.DIR_MINI_FILES_SDCARD_ROOT);
        SDKInit.initialize(this);
        initSpeech();
    }
    private void initSpeech(){
        StringBuffer param = new StringBuffer();
        try{
            param.append("appid=").append(getString(R.string.app_id));
            param.append(",");
            param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
            SpeechUtility.createUtility(this, param.toString());
            DebugLog.setLogLevel(DebugLog.LOG_LEVEL.none);
            FwLoggerFactory2.setup(
                    BuildConfig.DEBUG ? new AndroidLoggerFactory() : new InfrequentLoggerFactory());
            startService(new Intent(this, DemoMasterService.class));

            Log.i(MainActivity.TAG, "Speech App: Declaring speech settings");
            ServiceModules.initialize(this);
            ServiceModules.declare(SpeechSettings.class,
                    (aClass, moduleCreatedNotifier) -> {
                        moduleCreatedNotifier.notifyModuleCreated(
                                DemoSpeechJava.getInstance().createSpeechSettings());
                    });
            Log.i(MainActivity.TAG, "Speech App: Declaring speech service");
            ServiceModules.declare(SpeechService.class,
                    (aClass, moduleCreatedNotifier) -> ThreadPool.runOnNonUIThread(() -> {
                        while (DemoSpeechJava.getInstance().createSpeechService() == null) {
                            Log.i(MainActivity.TAG, "Speech App: Cannot get service");
                            SystemClock.sleep(5);
                        }
                        moduleCreatedNotifier.notifyModuleCreated(DemoSpeechJava.getInstance().createSpeechService());
                    }));
        } catch (Exception e) {
            Log.i(MainActivity.TAG, "Error: " + e);
        }
    }

//    @Override
//    protected void onStartFailed(UbtSkillInfo ubtSkillInfo) {
//
//    }
//
//    @Override
//    protected void onInterrupted() {
//
//    }

}
