package com.ubtrobot.mini.sdkdemo.speech;

import android.os.Handler;
import android.os.Looper;
import com.ubtrobot.async.Consumer2;
import com.ubtrobot.async.ListenerList2;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallProcessAdapter2;
import com.ubtrobot.master.adapter.ParcelableCallProcessAdapter2;
import com.ubtrobot.master.adapter.ParcelableParamParser2;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate2;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ParcelableCompetingCallDelegate2;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.parcelable.BoolValue;
import com.ubtrobot.service.ModuleCreatedNotifier;
import com.ubtrobot.service.ModuleNotFoundException;
import com.ubtrobot.service.ServiceModules;
import com.ubtrobot.speech.RecognitionException;
import com.ubtrobot.speech.RecognitionOption;
import com.ubtrobot.speech.RecognitionProgress;
import com.ubtrobot.speech.RecognitionResult;
import com.ubtrobot.speech.SpeakingVoiceList;
import com.ubtrobot.speech.SpeechCall;
import com.ubtrobot.speech.SpeechService;
import com.ubtrobot.speech.SynthesisException;
import com.ubtrobot.speech.SynthesisOption;
import com.ubtrobot.speech.SynthesisProgress;
import com.ubtrobot.speech.UnderstandingException;
import com.ubtrobot.speech.UnderstandingOption;
import com.ubtrobot.speech.UnderstandingResult;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.ulog.FwLoggerFactory2;
import com.ubtrobot.ulog.Logger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechSystemServiceTest extends MasterSystemService {
    private static final Logger LOGGER = FwLoggerFactory2.getLogger("SpeechSystemService");
    private volatile SpeechServiceTest mSpeechService;
    private ParcelableCallProcessAdapter2 mCallProcessor;
    private ParcelableCompetingCallDelegate2 mCompetingCallDelegate;
    private volatile SpeechCall recognizeCall;
    private ListenerList2<SpeechCall> callList;
    private final byte[] mLock = new byte[0];

    protected void onServiceCreate() {
        Handler handler = new Handler(Looper.getMainLooper());
        this.mCompetingCallDelegate = new ParcelableCompetingCallDelegate2(this, handler);
        this.mCallProcessor = new ParcelableCallProcessAdapter2();
        this.callList = new ListenerList2(handler);

        try {
            ServiceModules.getModuleCreator(SpeechServiceTest.class).createModule(SpeechServiceTest.class, new ModuleCreatedNotifier<SpeechServiceTest>() {
                public void notifyModuleCreated(SpeechServiceTest service) {
                    SpeechSystemServiceTest.this.mSpeechService = service;
                    synchronized(SpeechSystemServiceTest.this.mLock) {
                        if (SpeechSystemServiceTest.this.recognizeCall != null) {
                            try {
                                SpeechSystemServiceTest.this.recognizeCall.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            SpeechSystemServiceTest.this.recognizeCall = null;
                        }
                    }

                    SpeechSystemServiceTest.this.callList.forEach(new Consumer2<SpeechCall>() {
                        public void accept(SpeechCall call) {
                            try {
                                call.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            SpeechSystemServiceTest.this.callList.unregister(call);
                        }
                    });
                }
            });
        } catch (ModuleNotFoundException var3) {
            LOGGER.e("Service is unavailable due to SpeechService NOT found.", new Object[0]);
            handler.postDelayed(new Runnable() {
                public void run() {
                    synchronized(SpeechSystemServiceTest.this.mLock) {
                        if (SpeechSystemServiceTest.this.recognizeCall != null) {
                            SpeechSystemServiceTest.this.recognizeCall.error(500, "SpeechService NOT found.");
                            SpeechSystemServiceTest.this.recognizeCall = null;
                        }
                    }

                    SpeechSystemServiceTest.this.callList.forEach(new Consumer2<SpeechCall>() {
                        public void accept(SpeechCall call) {
                            call.error(500, "SpeechService NOT found.");
                            SpeechSystemServiceTest.this.callList.unregister(call);
                        }
                    });
                }
            }, 50L);
        }

    }

    protected List<CompetingItemDetail> getCompetingItems() {
        List<CompetingItemDetail> list = new LinkedList();
        list.add((new CompetingItemDetail.Builder("speech", "synthesizer")).setDescription("the synthesize competing item").addCallPath("/speech/synthesize").build());
        list.add((new CompetingItemDetail.Builder("speech", "recognizer")).setDescription("the recognize competing item").addCallPath("/speech/recognize").build());
        return list;
    }


    @Call(
            path = "/speech/recognize"
    )
    public void recognize(final Request request, final Responder responder) {
        LOGGER.i("recognize...", new Object[0]);
        final RecognitionOption option = (RecognitionOption)ParcelableParamParser2.parseParam(request, RecognitionOption.class, responder);
        if (option == null) {
            responder.respondFailure(new CallException(400, "param error."));
        } else {
            if (this.mSpeechService == null) {
                synchronized(this.mLock) {
                    if (this.recognizeCall != null) {
                        this.recognizeCall.cancel();
                    }

                    final AtomicBoolean running = new AtomicBoolean(true);
                    this.recognizeCall = new SpeechCall() {
                        public Void call() {
                            if (running.compareAndSet(true, false)) {
                                SpeechSystemServiceTest.this.doRecognizeCall(request, responder, option);
                            } else {
                                SpeechSystemServiceTest.LOGGER.w("cancel a recognize call.", new Object[0]);
                            }

                            return null;
                        }

                        public void error(int code, String msg) {
                            responder.respondFailure(code, msg);
                        }

                        public void cancel() {
                            if (running.compareAndSet(true, false)) {
                                responder.respondFailure(500, "recognize be canceled!!!");
                            }

                        }
                    };
                }
            } else {
                this.doRecognizeCall(request, responder, option);
            }

        }
    }

    private void doRecognizeCall(Request request, Responder responder, final RecognitionOption option) {
        this.mCompetingCallDelegate.onCall(request, "recognizer", responder, new CompetingCallDelegate2.SessionProgressiveCallable<RecognitionResult, RecognitionException, RecognitionProgress>() {
            public ProgressivePromise<RecognitionResult, RecognitionException, RecognitionProgress> call() {
                return SpeechSystemServiceTest.this.mSpeechService.recognize(option);
            }
        }, new CompetingCallDelegate2.FConverter<RecognitionException>() {
            public CallException convertFail(RecognitionException fail) {
                return new CallException(fail.getCode(), fail.getMessage());
            }
        });
    }

    @Call(
            path = "/speech/recognizing"
    )
    public void isRecognizing(Request request, Responder responder) {
        LOGGER.i("isRecognizing...", new Object[0]);
        if (this.mSpeechService == null) {
            responder.respondSuccess(ParcelableParam.create(BoolValue.FALSE));
        } else {
            responder.respondSuccess(ParcelableParam.create(this.mSpeechService.isRecognizing() ? BoolValue.TRUE : BoolValue.FALSE));
        }

    }

    @Call(
            path = "/speech/understand"
    )
    public void understand(Request request, final Responder responder) {
        LOGGER.i("understand...", new Object[0]);
        final UnderstandingOption param = (UnderstandingOption)ParcelableParamParser2.parseParam(request, UnderstandingOption.class, responder);
        if (param == null) {
            responder.respondFailure(new CallException(400, "param error."));
        } else {
            if (this.mSpeechService == null) {
                synchronized(this.mLock) {
                    this.callList.register(new SpeechCall() {
                        public void cancel() {
                        }

                        public Void call() {
                            SpeechSystemServiceTest.this.doUnderstandCall(responder, param);
                            return null;
                        }

                        public void error(int code, String msg) {
                            responder.respondFailure(code, msg);
                        }
                    });
                }
            } else {
                this.doUnderstandCall(responder, param);
            }

        }
    }

    private void doUnderstandCall(Responder responder, final UnderstandingOption param) {
        this.mCallProcessor.onCall(responder, new CallProcessAdapter2.Callable<UnderstandingResult, UnderstandingException>() {
            public Promise<UnderstandingResult, UnderstandingException> call() {
                return SpeechSystemServiceTest.this.mSpeechService.understand(param);
            }
        }, new CallProcessAdapter2.FConverter<UnderstandingException>() {
            public CallException convertFail(UnderstandingException fail) {
                return new CallException(fail.getCode(), fail.getMessage());
            }
        });
    }

    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        this.mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }
}
