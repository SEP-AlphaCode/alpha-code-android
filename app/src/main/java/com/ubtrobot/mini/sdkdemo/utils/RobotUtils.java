package com.ubtrobot.mini.sdkdemo.utils;

import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.mini.voice.protos.VoiceProto;

public class RobotUtils {
    // Get VoiceProto.Source for music
    public static VoiceProto.Source getVoiceProtoSource() {
        return VoiceProto.Source.MUSIC;
    }

    // Get MasterContext for global context
    public static MasterContext getMasterContext() {
        Master master = Master.get();
        return master.getGlobalContext();
    }
}
