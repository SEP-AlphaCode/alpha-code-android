package com.ubtrobot.mini.sdkdemo.speech;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.speech.AbstractWakeUpDetector;
import com.ubtrobot.speech.CompositeSpeechService;
import com.ubtrobot.speech.RecognitionException;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionProgress;
import com.ubtrobot.speech.RecognitionResult;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.SpeakingVoice;
import com.ubtrobot.speech.SynthesisException;
import com.ubtrobot.speech.SynthesisOption;
import com.ubtrobot.speech.SynthesisProgress;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.Understander;
import com.ubtrobot.speech.UnderstandingException;
import com.ubtrobot.speech.UnderstandingOption;
import com.ubtrobot.speech.UnderstandingResult;
import com.ubtrobot.speech.WakeUpDetector;
import com.ubtrobot.speech.WakeUpListener;

import java.util.List;

public class CompositeSpeechServiceTest implements SpeechServiceTest {
    private WakeUpDetector mWakeUpDetector;
    private Synthesizer mSynthesizer;
    private Recognizer mRecognizer;
    private Understander mUnderstander;

    private CompositeSpeechServiceTest() {
    }

    public ProgressivePromise<RecognitionResult, RecognitionException, RecognitionProgress> recognize(RecognitionOption option) {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setRecognizer()");
        } else {
            return this.mRecognizer.recognize(option);
        }
    }

    public boolean isRecognizing() {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setRecognizer()");
        } else {
            return this.mRecognizer.isRecognizing();
        }
    }

    public ProgressivePromise<Void, SynthesisException, SynthesisProgress> synthesize(SynthesisOption option) {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setSynthesizer()");
        } else {
            return this.mSynthesizer.synthesize(option);
        }
    }

    public boolean isSynthesizing() {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setSynthesizer()");
        } else {
            return this.mSynthesizer.isSynthesizing();
        }
    }

    public List<SpeakingVoice> getSpeakingVoiceList() {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setSynthesizer()");
        } else {
            return this.mSynthesizer.getSpeakingVoiceList();
        }
    }

    public Promise<UnderstandingResult, UnderstandingException> understand(UnderstandingOption option) {
        if (this.mRecognizer == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setUnderstander()");
        } else {
            return this.mUnderstander.understand(option);
        }
    }

    public void registerListener(WakeUpListener listener) {
        if (this.mWakeUpDetector == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setWakeUpDetector()");
        } else {
            this.mWakeUpDetector.registerListener(listener);
        }
    }

    public void unregisterListener(WakeUpListener listener) {
        if (this.mWakeUpDetector == null) {
            throw new UnsupportedOperationException("Did NOT invoke CompositeSpeechService.Builder.setWakeUpDetector()");
        } else {
            this.mWakeUpDetector.unregisterListener(listener);
        }
    }

    public static class Builder {
        private WakeUpDetector wakeUpDetector;
        private Recognizer recognizer;
        private Understander understander;

        public CompositeSpeechServiceTest.Builder setWakeUpDetector(AbstractWakeUpDetector wakeUpDetector) {
            this.wakeUpDetector = wakeUpDetector;
            return this;
        }


        public CompositeSpeechServiceTest.Builder setRecognizer(Recognizer recognizer) {
            this.recognizer = recognizer;
            return this;
        }

        public CompositeSpeechServiceTest.Builder setUnderstander(Understander understander) {
            this.understander = understander;
            return this;
        }

        public CompositeSpeechServiceTest build() {
            CompositeSpeechServiceTest service = new CompositeSpeechServiceTest();
            service.mWakeUpDetector = this.wakeUpDetector;
            service.mRecognizer = this.recognizer;
            service.mUnderstander = this.understander;
            return service;
        }
    }
}
