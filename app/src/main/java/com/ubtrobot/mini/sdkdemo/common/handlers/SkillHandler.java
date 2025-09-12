package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtechinc.skill.SkillApi;
import com.ubtechinc.skill.SkillHelper;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;

public class SkillHandler {
    private static final String TAG = "SkillHandler";
    private SkillApi skillApi;
    private SkillHelper skillHelper;

    public SkillHandler() {
        this.skillApi = SkillApi.get();
        this.skillHelper = new SkillHelper();
    }

    public void handleSkill() {
        skillApi.startSkill(SkillApi.SKILL_NAME.DANCE_SKIRT, null, new ResponseListener<Void>() {
            @Override
            public void onResponseSuccess(Void aVoid) {
                Log.i(TAG,"onResponseSuccess");
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.i(TAG,"onFailure i = " + i);
            }
        });
    }

    public void handleSkillHelper(String skillIntentName) {
        if (skillIntentName != null) {
            skillHelper.startSkillByIntent(skillIntentName, null, new ResponseCallback() {
                @Override
                public void onResponse(Request request, Response response) {
                    Log.i(TAG, "start success.");
                }

                @Override
                public void onFailure(Request request, CallException e) {
                    Log.i(TAG, e.getMessage());
                }
            });
        }
    }
}
