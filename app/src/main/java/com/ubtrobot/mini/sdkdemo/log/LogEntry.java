package com.ubtrobot.mini.sdkdemo.log;

public class LogEntry {
    private String robotId;
    private String level;
    private String tag;
    private String message;
    private long timestamp;

    public LogEntry(String robotId, String level, String tag, String message, long timestamp) {
        this.robotId = robotId;
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public String getRobotId() { return robotId; }
    public String getLevel() { return level; }
    public String getTag() { return tag; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setRobotId(String robotId) { this.robotId = robotId; }
    public void setLevel(String level) { this.level = level; }
    public void setTag(String tag) { this.tag = tag; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String toJson() {
        return "{"
                + "\"robotId\":\"" + robotId + "\","
                + "\"level\":\"" + level + "\","
                + "\"tag\":\"" + tag + "\","
                + "\"message\":\"" + message + "\","
                + "\"timestamp\":" + timestamp
                + "}";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Tag: %s, Robot: %s, Time: %d)",
                level, message, tag, robotId,  timestamp);
    }
}
