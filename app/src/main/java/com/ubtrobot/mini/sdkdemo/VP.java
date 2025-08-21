package com.ubtrobot.mini.sdkdemo;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.call.CallConfiguration;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.mini.properties.sdk.PropertiesApi;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.protos.VoiceProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;
import com.ubtrobot.transport.message.StickyResponseCallback;

import java.io.File;

public class VP {
    private final ServiceProxy voice;

    private VP() {
        this.voice = Master.get().getGlobalContext().createSystemServiceProxy("voice");
        this.voice.setConfiguration((new CallConfiguration.Builder()).suppressSyncCallOnMainThreadWarning(true).build());
    }

    public static VP get() {
        return VP.Holder._pool;
    }

    public void playTTs(String text, Priority priority, final Listener listener) {
        if (text != null && !TextUtils.isEmpty(text.trim())) {
            this.voice.callStickily("/playTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setData(text).setType(VoiceProto.Type.TTS).setPriority(priority.name()).build()), new StickyResponseCallback() {
                public void onResponseStickily(Request request, Response response) {
                }

                public void onResponseCompletely(Request request, Response response) {
                    if (listener != null) {
                        listener.onCompleted();
                    }

                }

                public void onFailure(Request request, CallException e) {
                    if (listener != null) {
                        listener.onError(request, e);
                    }

                }
            });
        } else {
            if (listener != null) {
                listener.onError(null, null);
            }

        }
    }

    public void playLocalTTs(File file, Priority priority, final VoiceListener listener) {
        this.voice.callStickily("/playTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setData(file.getPath()).setType(VoiceProto.Type.FILE).setPriority(priority.name()).build()), new StickyResponseCallback() {
            public void onResponseStickily(Request request, Response response) {
            }

            public void onResponseCompletely(Request request, Response response) {
                if (listener != null) {
                    listener.onCompleted();
                }

            }

            public void onFailure(Request request, CallException e) {
                if (listener != null) {
                    listener.onError(e.getCode(), e.getMessage());
                }

            }
        });
    }

    public void playLocalTTs(String mp3FileName, Priority priority, VoiceListener listener) {
        this.playLocalTTs(new File(PropertiesApi.findSystemTTsPath(mp3FileName)), priority, listener);
    }

    public void stopTTs(Priority priority, @Nullable final ResponseListener<Void> listener) {
        this.voice.call("/stopTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setPriority(priority.name()).build()), new ResponseCallback() {
            public void onResponse(Request request, Response response) {
                if (listener != null) {
                    listener.onResponseSuccess(null);
                }

            }

            public void onFailure(Request request, CallException e) {
                if (listener != null) {
                    listener.onFailure(e.getCode(), e.getMessage());
                }

            }
        });
    }

    public void stopLocalTTs(String ttsName, Priority priority, @Nullable final ResponseListener<Void> listener) {
        this.voice.call("/stopLocalTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setData(PropertiesApi.findSystemTTsPath(ttsName)).setPriority(priority.name()).build()), new ResponseCallback() {
            public void onResponse(Request request, Response response) {
                if (listener != null) {
                    listener.onResponseSuccess(null);
                }

            }

            public void onFailure(Request request, CallException e) {
                if (listener != null) {
                    listener.onFailure(e.getCode(), e.getMessage());
                }

            }
        });
    }

    public void playUnsafeTTs(String mp3FileName, @Nullable final VoiceListener listener) {
        this.voice.callStickily("/playUnsafeLocalTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setData(PropertiesApi.findSystemTTsPath(mp3FileName)).build()), new StickyResponseCallback() {
            public void onResponseStickily(Request request, Response response) {
            }

            public void onResponseCompletely(Request request, Response response) {
                if (listener != null) {
                    listener.onCompleted();
                }

            }

            public void onFailure(Request request, CallException e) {
                if (listener != null) {
                    listener.onError(e.getCode(), e.getMessage());
                }

            }
        });
    }

    public void playUnsafeTTs(File file, @Nullable final VoiceListener listener) {
        this.voice.callStickily("/playUnsafeLocalTTs", ProtoParam.create(VoiceProto.BriefVoice.newBuilder().setData(file.getAbsolutePath()).build()), new StickyResponseCallback() {
            public void onResponseStickily(Request request, Response response) {
            }

            public void onResponseCompletely(Request request, Response response) {
                if (listener != null) {
                    listener.onCompleted();
                }

            }

            public void onFailure(Request request, CallException e) {
                if (listener != null) {
                    listener.onError(e.getCode(), e.getMessage());
                }

            }
        });
    }

    private static class Holder {
        private static VP _pool = new VP();
    }
    public interface Listener {
        void onCompleted();

        void onError(Request req, CallException var2);
    }

}
