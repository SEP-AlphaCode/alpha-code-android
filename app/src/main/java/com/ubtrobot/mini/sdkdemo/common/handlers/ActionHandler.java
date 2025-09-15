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
                    LogManager.log(LogLevel.INFO, TAG,"Action " + actionCode + " done!");
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    Log.e(TAG, "Action " + actionCode + " failed: " + s);
                    LogManager.log(LogLevel.ERROR, TAG,"Action " + actionCode + " failed: " + s);
                }
            });
        }
    }
}
