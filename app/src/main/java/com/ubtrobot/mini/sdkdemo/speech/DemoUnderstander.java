package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtrobot.async.CancelledException;
import com.ubtrobot.async.Deferred;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.speech.AbstractUnderstander;
import com.ubtrobot.speech.UnderstandingException;
import com.ubtrobot.speech.UnderstandingOption;
import com.ubtrobot.speech.UnderstandingResult;

public class DemoUnderstander extends AbstractUnderstander {
    public DemoUnderstander() {
        Log.i(MainActivity.TAG, "Init Understander");
    }

    @Override
    protected void understand(UnderstandingOption understandingOption,
                              Deferred<UnderstandingResult, UnderstandingException> deferred) {
        Log.i(MainActivity.TAG, "Understander: Understanding...");
        try {
            Log.i(MainActivity.TAG, "Understander: Deferred: " + deferred.get().toString());
        } catch (UnderstandingException e) {
            Log.i(MainActivity.TAG, "Understander: Deferred error: " + e);
        } catch (CancelledException e) {
            Log.i(MainActivity.TAG, "Understander: Cancelled");
        }
    }
}
