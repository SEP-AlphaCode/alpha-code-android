package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;

public class ActionHandler {
    private static final String TAG = "ActionHandler";
    private ActionApi actionApi;

    public ActionHandler() {
        this.actionApi = ActionApi.get();
    }

    public void handleAction(String actionCode) {
        if (actionCode != null) {
            actionApi.playAction(actionCode, new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    Log.i(TAG, "Action " + actionCode + " done!");
                    // Log tự động có accountLessonId nếu đang trong submission
                    LogManager.log(LogLevel.INFO, "action", "Action " + actionCode + " done!", "action", actionCode);
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.e(TAG, "Action " + actionCode + " failed: " + s);
                    LogManager.log(LogLevel.ERROR, "action", "Action " + actionCode + " failed: " + s, "action", actionCode);
                }
            });
        }
    }

    public void handleAction(String actionCode, ResponseListener<Void> listener) {
        if (actionCode != null) {
            actionApi.playAction(actionCode, new ResponseListener<Void>() {
                @Override
                public void onResponseSuccess(Void aVoid) {
                    Log.i(TAG, "Action " + actionCode + " done!");
                    LogManager.log(LogLevel.INFO, "action", "Action " + actionCode + " done!", "action", actionCode);
                    listener.onResponseSuccess(aVoid);
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.e(TAG, "Action " + actionCode + " failed: " + s);
                    LogManager.log(LogLevel.ERROR, "action", "Action " + actionCode + " failed: " + s, "action", actionCode);
                    listener.onFailure(i, s);
                }
            });
        }
    }
}
