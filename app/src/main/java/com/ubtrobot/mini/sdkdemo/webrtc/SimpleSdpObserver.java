package com.ubtrobot.mini.sdkdemo.webrtc;

import android.util.Log;
import org.webrtc.*;

public class SimpleSdpObserver implements SdpObserver {
    @Override public void onCreateSuccess(SessionDescription sdp) {}
    @Override public void onSetSuccess() {}
    @Override public void onCreateFailure(String s) { Log.e("SDP", "create fail: " + s); }
    @Override public void onSetFailure(String s) { Log.e("SDP", "set fail: " + s); }
}
