package com.ubtrobot.mini.sdkdemo.utils;

import android.graphics.Color;
import android.util.Log;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;

public class LedHelper {
    private MouthLedApi mouthLedApi;

    public LedHelper() {
        mouthLedApi = MouthLedApi.get();
    }

    private static final String TAG = "LedHelper";
    /**
     * 0 = OK (green), 1 = WAIT (yellow), 2 = FAIL (red)
     */
    public void notifyState(int state) {
        int color = -1;
        switch (state) {
            case 0:
                color = Color.argb(0, 0, 255, 0);
                break;
            case 1:
                color = Color.argb(0, 255, 255, 0);
                break;
            case 2:
                color = Color.argb(0, 255, 0, 0);
                break;
        }
        Log.i(TAG, "State is " + state);
        mouthLedApi.startNormalModel(color, 3000, Priority.HIGH, null);
    }
}
