package com.ubtrobot.mini.sdkdemo.models;

public class RobotRequestTypes {
    /**
     * Required field: asr
     */
    public static final String PROCESS_SPEECH = "process_speech";
    /**
     * Required fields:
     * 1. image
     * 2. params
     * - lang: target language (en, vi)
     */
    public static final String DETECT_OBJECT = "detect_object";
    /**
     * Required fields: image
     */
    public static final String PARSE_OSMO = "parse_osmo";
    /**
     * Required fields: params
     * - serial: Serial of the robot
     */
    public static final String NOTIFY_SHUTDOWN = "notify_shutdown";
    /**
     * Required fields: image
     */
    public static final String PARSE_QR = "parse_qr";
    /**
     * A boot time ping to establish the connection
     */
    public static final String PING = "ping";
}
