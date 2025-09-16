package com.ubtrobot.mini.sdkdemo.models.response;

import com.ubtrobot.masterevent.protos.SysMasterEvent;

public class BatteryStatusResponse {
    private int status;
    private int level;
    private int levelStatus;
    private String batteryType;

    public String getBatteryType() {
        return batteryType;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelStatus() {
        return levelStatus;
    }

    public int getStatus() {
        return status;
    }
    public BatteryStatusResponse(int status, int level, int levelStatus, String batteryType){
        this.status = status;
        this.level = level;
        this.levelStatus = levelStatus;
        this.batteryType = batteryType;
    }
    public static BatteryStatusResponse fromProto(SysMasterEvent.BatteryStatusData proto) {
        return new BatteryStatusResponse(
                proto.getStatus(),
                proto.getLevel(),
                proto.getLevelStatus(),
                proto.getBatteryType().name() // convert enum to String
        );
    }
}
