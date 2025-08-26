package com.ubtrobot.mini.sdkdemo.speech;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.Utils;
import com.ubtech.utilcode.utils.notification.NotificationCenter;
import com.ubtech.utilcode.utils.thread.ThreadPool;
import com.ubtechinc.mini.weinalib.TencentVadRecorder;
import com.ubtechinc.mini.weinalib.WeiNaMicApi;
import com.ubtechinc.mini.weinalib.WeiNaRecorder;
import com.ubtechinc.mini.weinalib.wakeup.WeiNaWakeUpDetector;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.mini.iflytek.wakeup.IflytekWakeUpDetector;
import com.ubtrobot.mini.sdkdemo.ActionApiActivity;
import com.ubtrobot.mini.sdkdemo.MainActivity;
import com.ubtrobot.mini.sdkdemo.R;
import com.ubtrobot.mini.sdkdemo.TakePicApiActivity;
import com.ubtrobot.mini.sdkdemo.speech.custom.VoskRecognizerWrapper;
import com.ubtrobot.mini.speech.framework.DingDangManager;
import com.ubtrobot.mini.speech.framework.ResourceLoader;
import com.ubtrobot.mini.speech.framework.ServiceConstants;
import com.ubtrobot.mini.speech.framework.SpeechModuleFactory;
import com.ubtrobot.mini.speech.framework.SpeechSettingStub;
import com.ubtrobot.mini.speech.framework.WakeupAudioPlayer;
import com.ubtrobot.mini.speech.framework.skill.SkillManager;
import com.ubtrobot.mini.speech.framework.utils.MicApiHelper;
import com.ubtrobot.mini.speech.framework.utils.ShakeHeadUtils;
import com.ubtrobot.motor.MotorApi;
import com.ubtrobot.parcelable.BaseProgress;
import com.ubtrobot.speech.AbstractRecognizer;
import com.ubtrobot.speech.AbstractSynthesizer;
import com.ubtrobot.speech.AbstractUnderstander;
import com.ubtrobot.speech.CompositeSpeechService;
import com.ubtrobot.speech.RecognitionException;
import com.ubtrobot.speech.RecognitionProgress;
import com.ubtrobot.speech.RecognitionResult;
import com.ubtrobot.speech.RecognizerListener;
import com.ubtrobot.speech.SpeechConstants;
import com.ubtrobot.speech.SynthesisException;
import com.ubtrobot.speech.SynthesisProgress;
import com.ubtrobot.speech.SynthesizerListener;
import com.ubtrobot.speech.UnderstanderListener;
import com.ubtrobot.speech.UnderstandingException;
import com.ubtrobot.speech.UnderstandingResult;
import com.ubtrobot.speech.WakeUp;
import com.ubtrobot.speech.parcelable.ASRState;
import com.ubtrobot.speech.parcelable.AccessToken;
import com.ubtrobot.speech.parcelable.InitResult;
import com.ubtrobot.speech.parcelable.MicrophoneWakeupAngle;
import com.ubtrobot.speech.parcelable.TTsState;
import com.ubtrobot.speech.protos.Speech;
import com.ubtrobot.ulog.FwLoggerFactory2;

import java.io.IOException;

public final class DemoSpeechJava extends SpeechModuleFactory {
    private static final String SMALL_MODEL = "vosk-model-small-en-us-0.15";

    private static final DemoSpeechJava INSTANCE = new DemoSpeechJava();

    private static final String TAG = "SpeechFactory";
    private static final com.ubtrobot.ulog.Logger LOGGER = FwLoggerFactory2.getLogger("Speech-Chain");

    private final android.content.Context appContext = Utils.getContext().getApplicationContext();
    private final SpeechSettingStub speechSettingStub = new SpeechSettingStub(appContext);

    private AbstractRecognizer recognizer;
    private AbstractSynthesizer synthesizer;
    private AbstractUnderstander understander;
    private CompositeSpeechService speechServiceStub;

    private RecognizerListener mRecognizerListener;
    private SynthesizerListener mSynthesizerListener;
    private UnderstanderListener mUnderstanderListener;

    private final SkillManager mSkillManager = new SkillManager();
    private TakePicApiActivity takePicApiActivity;
    private ActionApiActivity actionApiActivity;

    private DemoSpeechJava() {
        // Private constructor for singleton
    }


    private DemoSpeechJava(TakePicApiActivity takePicApiActivity, ActionApiActivity actionApiActivity) {
        this.takePicApiActivity = takePicApiActivity;
        this.actionApiActivity = actionApiActivity;
    }

    public static DemoSpeechJava getInstance() {
        return INSTANCE;
    }

    private void shakeHead() {
        final short lastLockAngle = MicApiHelper.getMicLockAngle();
        LOGGER.d("shakeHead ---lastLockAngel = " + lastLockAngle);

        ShakeHeadUtils.MoveHeadCallback callback = new ShakeHeadUtils.MoveHeadCallback() {
            @Override
            public void onProgress(int moveAngel, int currmotorAngel) {
                short lastLockAngle = MicApiHelper.getMicLockAngle();
                short newMicAngle = (short) ((360 + lastLockAngle + moveAngel) % 360);
                MicApiHelper.setMicLockAngle(newMicAngle, true);
            }

            @Override
            public void onSucc(int moveAngel, int currMotorAngel) {
                short lastLockAngle = MicApiHelper.getMicLockAngle();
                short newMicAngle = (short) ((360 + lastLockAngle + moveAngel) % 360);
                MicApiHelper.setMicLockAngle(newMicAngle, true);
                LOGGER.d("onSucc---lastLockAngelxx = " + lastLockAngle +
                        ", moveAngel = " + moveAngel + ", finalMicAngle = " +
                        MicApiHelper.getMicLockAngle());
            }

            @Override
            public void onError(int moveAngel, int currMotorAngel) {
                short lastLockAngle = MicApiHelper.getMicLockAngle();
                short newMicAngle = (short) ((360 + lastLockAngle + moveAngel) % 360);
                MicApiHelper.setMicLockAngle(newMicAngle, true);
            }
        };
        ShakeHeadUtils.shakeHead(lastLockAngle, callback);
    }

    public void destroy() {
        WeiNaMicApi.get().release();
    }

    public void init(final MasterSystemService service) {
        // Load the wake-up sound effect in advance
        WakeupAudioPlayer.get(appContext);

        final MasterSystemService hostService = service;

        WeiNaMicApi.get().addDoaAngleCallback(angle -> {
            hostService.publishCarefully(
                    ServiceConstants.PATH_MICROPHONE_ARRAY_WAKEUP_ANGLE,
                    ParcelableParam.create(new MicrophoneWakeupAngle((int) angle))
            );

            MicApiHelper.setMicLockAngle(angle, false);
            if (createSpeechSettings().isSpeechLinkable()
                    && ShakeHeadUtils.shakeHeadTiming == ShakeHeadUtils.ShakeHeadTiming.BeforeRecord) {
                if (!ActionApi.get().unsafeAction()) {
                    shakeHead();
                }
            }
        });

        mRecognizerListener = new RecognizerListener() {
            @Override
            public void onRecognizingFailure(RecognitionException e) {
                int code = (e.getExtCode() != 0) ? e.getExtCode() : e.getCode();
                hostService.publishCarefully(
                        ServiceConstants.ACTION_SPEECH_ASR_STATE,
                        ParcelableParam.create(new ASRState(e.getMessage(), code))
                );
                LogUtils.w(TAG, "onRecognizingFailure:(code=" + e.getCode() + ", extCode = " +
                        e.getExtCode() + ", msg=" + e.getMessage() + ")");
            }

            @Override
            public void onRecognizingResult(RecognitionResult recognitionResult) {
            }

            @Override
            public void onRecognizingProgress(RecognitionProgress progress) {
                if (progress != null) {
                    switch (progress.getProgress()) {
                        case BaseProgress.PROGRESS_BEGAN:
                            hostService.publishCarefully(
                                    ServiceConstants.ACTION_SPEECH_ASR_STATE,
                                    ParcelableParam.create(new ASRState(BaseProgress.PROGRESS_BEGAN))
                            );
                            break;

                        case BaseProgress.PROGRESS_ENDED:
                            if (recognizer != null && recognizer.isRecognizing()) {
                                hostService.publishCarefully(
                                        ServiceConstants.ACTION_SPEECH_ASR_STATE,
                                        ParcelableParam.create(new ASRState(BaseProgress.PROGRESS_ENDED))
                                );
                            }

                            if (createSpeechSettings().isSpeechLinkable()
                                    && ShakeHeadUtils.shakeHeadTiming == ShakeHeadUtils.ShakeHeadTiming.AfterRecord) {
                                if (!ActionApi.get().unsafeAction()) {
                                    shakeHead();
                                }
                            }
                            mSkillManager.stopSkill(SkillManager.SKILL_AUDIORECORD);
                            break;
                    }
                }
            }
        };

        mSynthesizerListener = new SynthesizerListener() {
            @Override
            public void onSynthesizingProgress(SynthesisProgress progress) {
                if (progress != null) {
                    switch (progress.getProgress()) {
                        case BaseProgress.PROGRESS_BEGAN:
                            hostService.publishCarefully(
                                    ServiceConstants.ACTION_SPEECH_TTS_STATE,
                                    ParcelableParam.create(new TTsState(BaseProgress.PROGRESS_BEGAN))
                            );
                            break;

                        case BaseProgress.PROGRESS_ENDED:
                            hostService.publishCarefully(
                                    ServiceConstants.ACTION_SPEECH_TTS_STATE,
                                    ParcelableParam.create(new TTsState(BaseProgress.PROGRESS_ENDED))
                            );
                            mSkillManager.stopSkill(SkillManager.SKILL_CHAT);
                            break;
                    }
                }
            }

            @Override
            public void onSynthesizingResult() {
            }

            @Override
            public void onSynthesizingFailure(SynthesisException e) {
                LogUtils.w(TAG, "onSynthesizingFailure " + e);
                mSkillManager.stopSkill(SkillManager.SKILL_CHAT);
            }
        };

        mUnderstanderListener = new UnderstanderListener() {
            @Override
            public void onUnderstandingFailure(UnderstandingException e) {
                int code = (e.getExtCode() != 0) ? e.getExtCode() : e.getCode();
                if (code == 403) {
                    hostService.publishCarefully(
                            ServiceConstants.ACTION_SPEECH_ASR_STATE,
                            ParcelableParam.create(new ASRState(e.getMessage(), ASRState.CODE_UNAUTHENTICATED))
                    );
                } else {
                    hostService.publishCarefully(
                            ServiceConstants.ACTION_SPEECH_ASR_STATE,
                            ParcelableParam.create(new ASRState(e.getMessage(), e.getCode()))
                    );
                }
                LogUtils.w(TAG, "onUnderstandingFailure:(code=" + e.getCode() + ", extCode = " +
                        e.getExtCode() + ", msg=" + e.getMessage() + ")");
            }

            @Override
            public void onUnderstandingResult(UnderstandingResult result) {
                hostService.publishCarefully(
                        ServiceConstants.ACTION_SPEECH_ASR_STATE,
                        ParcelableParam.create(new ASRState("recognized"))
                );
            }
        };

//        final WeiNaWakeUpDetector wakeUpDetector = new WeiNaWakeUpDetector(new WeiNaRecorder(false));
//        wakeUpDetector.registerListener(wakeUp -> {
//            handleWakeup(hostService, wakeUp, service);
//        });

        DingDangManager.INSTANCE.load(appContext, success -> {
            if (success) {
//                TencentVadRecorder asrRecorder = new TencentVadRecorder(ResourceLoader.INSTANCE.getVad_path());
//                recognizer = new DemoRecognizer(asrRecorder, takePicApiActivity, actionApiActivity);
//                recognizer.registerListener(mRecognizerListener);
                try {
                    String modelPath = VoskRecognizerWrapper.copyAssets(appContext, "vosk/" + SMALL_MODEL);
                    recognizer = new VoskRecognizerWrapper(appContext, modelPath);
                    recognizer.registerListener(mRecognizerListener);
                    synthesizer = new DemoSynthesizer();
                    synthesizer.registerListener(mSynthesizerListener);

                    understander = new DemoUnderstander();
                    understander.registerListener(mUnderstanderListener);

                    speechServiceStub = new CompositeSpeechService.Builder()
                            .setRecognizer(recognizer)
                            .setSynthesizer(synthesizer)
                            .setUnderstander(understander)
                            //.setWakeUpDetector(wakeUpDetector)
                            .build();

                    hostService.publishCarefully(
                            ServiceConstants.ACTION_SPEECH_INIT_RESULT,
                            ParcelableParam.create(new InitResult(0))
                    );

                    hostService.publishCarefully(
                            ServiceConstants.ACTION_SPEECH_WAKEUP,
                            ProtoParam.create(Speech.WakeupParam.newBuilder().build())
                    );

                    hostService.publishCarefully(SpeechConstants.ACTION_WAKE_UP,
                            ParcelableParam.create((new WakeUp.Builder()).build()));

                    NotificationCenter.defaultCenter().publish(
                            ServiceConstants.PATH_MICROPHONE_ARRAY_INIT_RESULT, DemoSpeechJava.this
                    );
                    Log.i(MainActivity.TAG, "311: init success.");
                } catch (IOException e) {
                    Log.e(MainActivity.TAG, "Failed to initialize DingDang: " + e.getMessage());
                }
            } else {
                Log.e(MainActivity.TAG,"Initialization configuration of wake-up module failed, restart application...");
                Process.killProcess(Process.myPid());
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    private void handleWakeup(MasterSystemService hostService, WakeUp wakeUp, MasterSystemService service) {
        LOGGER.w("publish wakeup.");
        hostService.publishCarefully(
                ServiceConstants.ACTION_SPEECH_WAKEUP,
                ProtoParam.create(Speech.WakeupParam.newBuilder().build())
        );

        hostService.publishCarefully(SpeechConstants.ACTION_WAKE_UP,
                ParcelableParam.create(wakeUp));

        WakeupAudioPlayer.get(appContext).play();

        ThreadPool.runOnNonUIThread(new Runnable() {
            @Override
            public void run() {
                MotorApi.get().clearProtectFlag(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
            }
        });
    }

    @Override
    public CompositeSpeechService createSpeechService() {
        return speechServiceStub;
    }

    @Override
    public SpeechSettingStub createSpeechSettings() {
        return speechSettingStub;
    }

    @Override
    public void refreshUnderstanderCode(AccessToken token, Callback callback) {
        if (!TextUtils.isEmpty(token.getCode()) && !TextUtils.isEmpty(token.getCodeVerifier())) {
            LOGGER.i("refresh Code: " + token.getCode() + ", codeVerifier:" + token.getCodeVerifier());
            // todo
        }
    }
}
