package com.ubtrobot.mini.sdkdemo.models.response;

public class SystemResponse {
    private String serialNumber;
    private String firmwareVersion;
    private String ctrlVersion;
    private String batteryInfo;

    public SystemResponse(String serialNumber, String firmwareVersion, String ctrlVersion, String batteryInfo) {
        this.serialNumber = serialNumber;
        this.firmwareVersion = firmwareVersion;
        this.ctrlVersion = ctrlVersion;
        this.batteryInfo = batteryInfo;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    public String getCtrlVersion() {
        return ctrlVersion;
    }
    public String getBatteryInfo() {
        return batteryInfo;
    }
}
