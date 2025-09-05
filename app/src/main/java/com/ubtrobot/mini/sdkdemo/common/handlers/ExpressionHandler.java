package com.ubtrobot.mini.sdkdemo.common.handlers;

import com.ubtrobot.express.ExpressApi;

public class ExpressionHandler {
    private static final String TAG = "ExpressionHandler";
    private ExpressApi expressApi;

    public ExpressionHandler() {
        this.expressApi = ExpressApi.get();
    }

    public void handleExpression(String code) {
        if (code != null) {
            expressApi.doExpress(code);
        }
    }
}
