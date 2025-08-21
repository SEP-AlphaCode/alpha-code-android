package com.ubtrobot.mini.sdkdemo.speech;

import android.util.Log;

import com.ubtrobot.async.Deferred;
import com.ubtrobot.speech.AbstractUnderstander;
import com.ubtrobot.speech.UnderstandingException;
import com.ubtrobot.speech.UnderstandingOption;
import com.ubtrobot.speech.UnderstandingResult;

public class DemoUnderstander extends AbstractUnderstander {
  @Override protected void understand(UnderstandingOption understandingOption,
      Deferred<UnderstandingResult, UnderstandingException> deferred) {
    Log.i("TAG", "Understand");
  }
}
