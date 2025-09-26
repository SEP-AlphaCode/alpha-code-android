package com.ubtrobot.mini.sdkdemo.custom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;

import com.ubtech.utilcode.utils.CollectionUtils;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.sauron.api.BundleParamUtils;
import com.ubtechinc.sauron.api.Constants;
import com.ubtechinc.sauron.api.ErrorCode;
import com.ubtechinc.sauron.api.FaceApi;
import com.ubtechinc.sauron.api.FaceFindListener;
import com.ubtechinc.sauron.api.FaceInfo;
import com.ubtechinc.sauron.api.FaceTrackListener;
import com.ubtechinc.sauron.api.ParcelableParamBuilder;
import com.ubtechinc.sauron.api.SkillUtils;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.component.ComponentBaseInfo;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.master.skill.SkillOpponent;
import com.ubtrobot.master.skill.SkillsProxy;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Event;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;
import com.ubtrobot.transport.message.StickyResponseCallback;

import java.util.List;

public class CustomFaceApi {
    public static final String TAG = "CustomFaceApi";
    private static final Uri uri = Uri.parse("content://com.ubtechinc.sauron.face");
    public static final int TYPE_SPEECH = 1;
    public static final int TYPE_IM = 2;
    private final ServiceProxy mServiceProxy;
    private FaceTrackListener faceTrackListener;
    private FaceFindListener faceFindListener;
    private EventReceiver faceTrackChangeReceiver;
    private EventReceiver faceTrackFinishReceiver;
    private EventReceiver faceFindChangeReceiver;
    private EventReceiver faceFindFinishReceiver;
    private final SkillsProxy mSkillsProxy;
    private static final CustomFaceApi instance;

    static {
        instance = new CustomFaceApi();
    }

    public static CustomFaceApi getInstance(){
        return instance;
    }
    private CustomFaceApi() {
        this.faceTrackChangeReceiver = new EventReceiver() {
            public void onReceive(MasterContext masterContext, Event event) {
                LogUtils.d("CustomFaceApi", "faceTrackChangeReceiver onReceive");
                if (CustomFaceApi.this.faceTrackListener != null) {
                    Bundle bundle = BundleParamUtils.getBundle(event.getParam());
                    if (bundle != null) {
                        List<FaceInfo> faceInfos = bundle.getParcelableArrayList("list");
                        if (CustomFaceApi.this.faceTrackListener != null) {
                            CustomFaceApi.this.faceTrackListener.onFaceChange(faceInfos);
                        }
                    }
                }

            }
        };
        this.faceTrackFinishReceiver = new EventReceiver() {
            public void onReceive(MasterContext masterContext, Event event) {
                LogUtils.d("CustomFaceApi", "faceTrackFinishReceiver onReceive");
                if (CustomFaceApi.this.faceTrackListener != null) {
                    CustomFaceApi.this.faceTrackListener.onStop();
                    CustomFaceApi.this.faceTrackListener = null;
                    CustomFaceApi.this.unSubscribeFaceTrack();
                }

            }
        };
        this.faceFindChangeReceiver = new EventReceiver() {
            public void onReceive(MasterContext masterContext, Event event) {
                LogUtils.d("CustomFaceApi", "faceFindChangeReceiver onReceive");
                if (CustomFaceApi.this.faceFindListener != null) {
                    Bundle bundle = BundleParamUtils.getBundle(event.getParam());
                    if (bundle != null) {
                        List<FaceInfo> faceInfos = bundle.getParcelableArrayList("list");
                        if (CustomFaceApi.this.faceFindListener != null) {
                            CustomFaceApi.this.faceFindListener.onFaceChange(faceInfos);
                        }
                    }
                }

            }
        };
        this.faceFindFinishReceiver = new EventReceiver() {
            public void onReceive(MasterContext masterContext, Event event) {
                LogUtils.d("CustomFaceApi", "faceFindFinishReceiver onReceive");
                if (CustomFaceApi.this.faceFindListener != null) {
                    Bundle bundle = BundleParamUtils.getBundle(event.getParam());
                    int code = bundle.getInt("resultCode");
                    if (code == 0) {
                        CustomFaceApi.this.faceFindListener.onStop();
                    } else {
                        CustomFaceApi.this.faceFindListener.onFail(code, ErrorCode.getMessage(code));
                    }

                    CustomFaceApi.this.faceFindListener = null;
                    CustomFaceApi.this.unSubscribeFaceFind();
                }

            }
        };
        this.mServiceProxy = Master.get().getGlobalContext().createServiceProxy(Utils.getContext().getPackageName(), "camera-common-service");
        this.mSkillsProxy = SkillUtils.buildSkillProxy();
    }
    public void apiFaceRegister(String userId, String name, final ResponseListener<String> responseListener) {
        StickyResponseCallback callback = new StickyResponseCallback() {
            public void onResponseStickily(Request request, Response response) {
                Log.i(TAG, "Response stickily");
                Log.d(TAG, "Request: " + request);
                Log.d(TAG, "Response: " + response);
            }

            public void onResponseCompletely(Request request, Response response) {
                Log.d(TAG, "startFaceRegister success ！！！");
                if (!response.getParam().isEmpty()) {
                    try {
                        Log.d(TAG, String.valueOf(request));
                        Log.d(TAG, String.valueOf(response));
                        ParcelableParam<Bundle> bundleParam = ParcelableParam.from(response.getParam(), Bundle.class);
                        Log.i(TAG, String.valueOf(bundleParam));
                        Bundle p = bundleParam.getParcelable();
                        Log.i(TAG, "Bundle contents:");

                        for (String key : p.keySet()) {
                            Object value = p.get(key);
                            Log.i(TAG, "Key: " + key + " | Value: " + value + " | Type: " +
                                    (value != null ? value.getClass().getSimpleName() : "null"));
                        }
                        int resultCode = ((Bundle)bundleParam.getParcelable()).getInt("resultCode");
                        if (resultCode == 0) {
                            String id = ((Bundle)bundleParam.getParcelable()).getString("id");
                            if (responseListener != null) {
                                responseListener.onResponseSuccess(id);
                            }
                        } else if (responseListener != null) {
                            responseListener.onFailure(resultCode, "fail");
                        }
                    } catch (ParcelableParam.InvalidParcelableParamException e) {
                        Log.e(TAG, "Error", e);
                    }
                } else if (responseListener != null) {
                    responseListener.onFailure(4, "fail");
                }

            }

            public void onFailure(Request request, CallException e) {
                Log.e(TAG, "Error", e);
                if (responseListener != null) {
                    if (e.getCode() == 403 && e.getSubCode() == 100) {
                        Integer code = null;

                        try {
                            SkillOpponent skillOpponent = (SkillOpponent)ParcelableParam.from(e.getParam(), SkillOpponent.class).getParcelable();
                            List<ComponentBaseInfo> components = skillOpponent.getSkillList();
                            List<Pair<ComponentBaseInfo, String>> stateList = skillOpponent.getServiceStateList();
                            if (!CollectionUtils.isEmpty(stateList)) {
                                for(Pair<ComponentBaseInfo, String> componentBaseInfoStringPair : stateList) {
                                    code = (Integer) Constants.skillStartFailMap.get(componentBaseInfoStringPair.second);
                                    if (code != null) {
                                        break;
                                    }
                                }
                            }

                            if (code == null && !CollectionUtils.isEmpty(components)) {
                                for(ComponentBaseInfo component : components) {
                                    code = (Integer)Constants.skillStartFailMap.get(component.getName());
                                    if (code != null) {
                                        break;
                                    }
                                }
                            }

                            if (code == null) {
                                code = 10;
                            }
                        } catch (ParcelableParam.InvalidParcelableParamException e1) {
                            e1.printStackTrace();
                            code = 10;
                        }

                        responseListener.onFailure(code, "fail");
                    } else {
                        responseListener.onFailure(4, "fail");
                    }
                }

            }
        };
        Param param = (new ParcelableParamBuilder()).putString("name", name).putString("userId", userId).putInt("type", 2).build();
        this.mServiceProxy.callStickily("/api/face/register", param, callback);
    }
    public void startRegister(String userId, String name, final ResponseListener<String> responseListener) {
        ResponseCallback callback = new ResponseCallback() {
            public void onResponse(Request request, Response response) {
                Log.d(TAG, "startRegister success ！！！");
                if (!response.getParam().isEmpty()) {
                    try {
                        ParcelableParam<Bundle> bundleParam = ParcelableParam.from(response.getParam(), Bundle.class);
                        int resultCode = ((Bundle)bundleParam.getParcelable()).getInt("resultCode");
                        if (resultCode == 0) {
                            String id = ((Bundle)bundleParam.getParcelable()).getString("id");
                            if (responseListener != null) {
                                responseListener.onResponseSuccess(id);
                            }
                        } else if (responseListener != null) {
                            responseListener.onFailure(resultCode, ErrorCode.getRegisterError(resultCode));
                        }
                    } catch (ParcelableParam.InvalidParcelableParamException e) {
                        Log.e(TAG, "Error: ", e);
                    }
                } else if (responseListener != null) {
                    responseListener.onFailure(4, ErrorCode.getRegisterError(4));
                }

            }

            public void onFailure(Request request, CallException e) {
                if (responseListener != null) {
                    if (e.getCode() == 403 && e.getSubCode() == 100) {
                        Integer code = null;

                        try {
                            SkillOpponent skillOpponent = (SkillOpponent)ParcelableParam.from(e.getParam(), SkillOpponent.class).getParcelable();
                            List<ComponentBaseInfo> components = skillOpponent.getSkillList();
                            List<Pair<ComponentBaseInfo, String>> stateList = skillOpponent.getServiceStateList();
                            if (!CollectionUtils.isEmpty(stateList)) {
                                for(Pair<ComponentBaseInfo, String> componentBaseInfoStringPair : stateList) {
                                    code = (Integer)Constants.skillStartFailMap.get(componentBaseInfoStringPair.second);
                                    if (code != null) {
                                        break;
                                    }
                                }
                            }

                            if (code == null && !CollectionUtils.isEmpty(components)) {
                                for(ComponentBaseInfo component : components) {
                                    code = (Integer)Constants.skillStartFailMap.get(component.getName());
                                    if (code != null) {
                                        break;
                                    }
                                }
                            }

                            if (code == null) {
                                code = 10;
                            }
                        } catch (ParcelableParam.InvalidParcelableParamException e1) {
                            e1.printStackTrace();
                            code = 10;
                        }

                        responseListener.onFailure(code, ErrorCode.getRegisterError(code));
                    } else {
                        responseListener.onFailure(4, ErrorCode.getRegisterError(4));
                    }
                }

            }
        };
        Param param = (new ParcelableParamBuilder()).putString("name", name).putString("userId", userId).putInt("type", 2).build();
        this.mSkillsProxy.call("/face/register", param, callback);
    }
    public void stopRegister(final String userId, final ResponseListener<Void> responseListener) {
        (new Thread() {
            public void run() {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userId);
                    Bundle result = Utils.getContext().getContentResolver().call(uri, "method_exit", (String)null, bundle);
                    result.setClassLoader(this.getClass().getClassLoader());
                    int resultCode = result.getInt("resultCode");
                    if (resultCode == 0) {
                        responseListener.onResponseSuccess(null);
                    } else {
                        responseListener.onFailure(resultCode, "fail");
                    }
                } catch (Exception var4) {
                    responseListener.onFailure(1, "fail");
                }

            }
        }).start();
    }
    private void unSubscribeFaceTrack() {
        Master.get().getGlobalContext().unsubscribe(this.faceTrackChangeReceiver);
        Master.get().getGlobalContext().unsubscribe(this.faceTrackFinishReceiver);
    }
    private void unSubscribeFaceFind() {
        Master.get().getGlobalContext().unsubscribe(this.faceFindChangeReceiver);
        Master.get().getGlobalContext().unsubscribe(this.faceFindFinishReceiver);
    }
}
