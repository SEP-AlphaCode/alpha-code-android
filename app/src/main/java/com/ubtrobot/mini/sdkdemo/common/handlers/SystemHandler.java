package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ubtrobot.mini.sdkdemo.models.response.SystemResponse;
import com.ubtrobot.mini.sysevent.EventApi;
import com.ubtrobot.mini.sysevent.SysEventApi;
import com.ubtrobot.sys.SysApi;
import com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager;

import org.json.JSONObject;

public class SystemHandler {
    private SysApi sysApi;
    private EventApi evtApi;
    private RobotSocketManager socketManager;

    private static final String TAG = "SystemHandler";

    public static SystemHandler get() {
        return Holder._api;
    }

    private static final class Holder {
        @SuppressLint({"StaticFieldLeak"})
        private static SystemHandler _api = new SystemHandler();
    }

    private SystemHandler() {
        sysApi = SysApi.get();
        evtApi = SysEventApi.get();
    }

    // cho phép set socketManager từ ngoài
    public void setSocketManager(RobotSocketManager socketManager) {
        this.socketManager = socketManager;
    }

    public String getSerialNumber() {
        return sysApi.readRobotSid();
    }

    public String getFirmwareVersion() {
        return sysApi.readFirmwareVersion();
    }

    public String getCtrlVersion() {
        return sysApi.readCtrlVersion();
    }

    public String getBatteryInfo() {
        return evtApi.getCurrentBatteryInfoSync().toString();
    }

    public SystemResponse getSystemInfo() {
        return new SystemResponse(
                getSerialNumber(),
                getFirmwareVersion(),
                getCtrlVersion(),
                getBatteryInfo()
        );
    }

    public void sendRobotStatus() {
        try {
            SystemResponse sysInfo = getSystemInfo();

            JSONObject data = new JSONObject();
            data.put("serialNumber", sysInfo.getSerialNumber());
            data.put("firmwareVersion", sysInfo.getFirmwareVersion());
            data.put("ctrlVersion", sysInfo.getCtrlVersion());
            data.put("batteryInfo", sysInfo.getBatteryInfo());

            JSONObject res = new JSONObject();
            res.put("type", "status_res");
            res.put("data", data);

            if (socketManager != null && socketManager.isConnected()) {
                socketManager.sendMessage(res.toString());
                Log.i(TAG, "Status sent: " + res.toString());
            } else {
                Log.w(TAG, "Socket not connected, cannot send status");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending robot status", e);
        }
    }
}
