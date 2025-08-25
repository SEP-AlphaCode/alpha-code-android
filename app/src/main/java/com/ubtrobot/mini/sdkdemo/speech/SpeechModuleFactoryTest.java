package com.ubtrobot.mini.sdkdemo.speech;

import com.ubtrobot.mini.speech.framework.SpeechModuleFactory;
import com.ubtrobot.mini.speech.framework.SpeechSettingStub;
import com.ubtrobot.speech.parcelable.AccessToken;

public abstract class SpeechModuleFactoryTest {
    public static final String KEY_NLP_CODE = "nlp_code";

    public abstract CompositeSpeechServiceTest createSpeechService();

    public abstract SpeechSettingStub createSpeechSettings();

    public abstract void refreshUnderstanderCode(AccessToken var1, Callback var2);

    public interface Callback {
        void successful();

        void failed(String var1);
    }
}
