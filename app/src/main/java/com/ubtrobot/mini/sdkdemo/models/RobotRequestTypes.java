package com.ubtrobot.mini.sdkdemo.models;

public class RobotRequestTypes {
    /**
     * Required field: asr
     */
    public static final String PROCESS_SPEECH = "process-speech";
    /**
     * Required fields: params
     * - text: Text to be processed
     */
    public static final String PROCESS_TEXT = "process-text";
    /**
     * Required fields:
     * 1. image
     * 2. params
     * - lang: target language (en, vi)
     */
    public static final String DETECT_OBJECT = "detect-object";
    /**
     * Required fields: image
     */
    public static final String PARSE_OSMO = "parse-osmo";
    /**
     * Required fields: image
     */
    public static final String PARSE_VIDEO_GENERATE = "parse-video";
    /**
     * Required fields: params
     * - serial: Serial of the robot
     */
    public static final String NOTIFY_SHUTDOWN = "notify-shutdown";
    /**
     * Required fields: image
     */
    public static final String PARSE_QR = "parse-qr";
    /**
     * A boot time ping to establish the connection
     */
    public static final String PING = "ping";
    /**
     * Required fields: params
     * - status: is coding block running (true, false)
     */
    public static final String CODING_BLOCK_STATUS = "coding-block-status";
}
