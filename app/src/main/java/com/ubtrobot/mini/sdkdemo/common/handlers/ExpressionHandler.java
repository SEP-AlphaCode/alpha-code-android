package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.util.Log;

import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.express.listeners.AnimationListener;

public class ExpressionHandler {
    private static final String TAG = "ExpressionHandler";
    private ExpressApi expressApi;

    public ExpressionHandler() {
        this.expressApi = ExpressApi.get();
    }

    public void handleExpression(String code) {
        if (code != null) {
            expressApi.doExpress(code, new AnimationListener() {
                @Override
                public void onAnimationStart() {
                    Log.i(TAG, "On anim start");
                }

                @Override
                public void onAnimationEnd(int i) {
                    Log.i(TAG, "On anim end: " + i);

                }

                @Override
                public void onAnimationRepeat(int i) {

                }
            });
        }
    }
    public void handleExpression(String code, AnimationListener listener) {
        if (code != null) {
            expressApi.doExpress(code, listener);
        }
    }
}
