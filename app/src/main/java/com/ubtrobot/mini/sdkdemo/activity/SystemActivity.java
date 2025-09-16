package com.ubtrobot.mini.sdkdemo.activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import com.ubtrobot.masterevent.protos.SysMasterEvent;
import com.ubtrobot.mini.sysevent.EventApi;
import com.ubtrobot.mini.sysevent.SysEventApi;
import com.ubtrobot.sys.SysApi;
public class SystemActivity {
    private SysApi sysApi;
    private EventApi evtApi;

    private static final String TAG = "SystemActivity";
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static SystemActivity get() {
        return SystemActivity.Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static SystemActivity _api = new SystemActivity();
    }

    private void initRobot() {
        sysApi = SysApi.get();
//        sysEventApi = (SysEventApi) SysEventApi.get();
        evtApi = SysEventApi.get();
    }

    public String getSerialNumber() {
        initRobot();
        return sysApi.readRobotSid();
    }

    public String getFirmwareVersion() {
        initRobot();
        return sysApi.readFirmwareVersion();
    }

    public String getCtrlVersion() {
        initRobot();
        return sysApi.readCtrlVersion();
    }

    public String getBatteryInfo() {
        initRobot();
        return evtApi.getCurrentBatteryInfoSync().toString();
    }

}
