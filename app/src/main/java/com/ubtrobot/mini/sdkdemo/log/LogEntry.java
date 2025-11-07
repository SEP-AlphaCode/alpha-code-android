package com.ubtrobot.mini.sdkdemo.log;

public class LogEntry {
    private String robotId;
    private String level;
    private String tag;
    private String message;
    private long timestamp;
    private String accountLessonId;  // ID bài nộp bài học
    private String type;             // "action", "speech", "emotion", "submission_start", "submission_end"
    private String code;             // Mã action code như "012", "027"

    public LogEntry(String robotId, String level, String tag, String message, long timestamp) {
        this.robotId = robotId;
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Constructor mở rộng với accountLessonId, type, code
    public LogEntry(String robotId, String level, String tag, String message, long timestamp,
                    String accountLessonId, String type, String code) {
        this.robotId = robotId;
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.timestamp = timestamp;
        this.accountLessonId = accountLessonId;
        this.type = type;
        this.code = code;
    }

    // Getters
    public String getRobotId() { return robotId; }
    public String getLevel() { return level; }
    public String getTag() { return tag; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getAccountLessonId() { return accountLessonId; }
    public String getType() { return type; }
    public String getCode() { return code; }

    // Setters
    public void setRobotId(String robotId) { this.robotId = robotId; }
    public void setLevel(String level) { this.level = level; }
    public void setTag(String tag) { this.tag = tag; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setAccountLessonId(String accountLessonId) { this.accountLessonId = accountLessonId; }
    public void setType(String type) { this.type = type; }
    public void setCode(String code) { this.code = code; }

    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"robotId\":\"").append(robotId).append("\",");
        json.append("\"level\":\"").append(level).append("\",");
        json.append("\"tag\":\"").append(tag).append("\",");
        json.append("\"message\":\"").append(message).append("\",");
        json.append("\"timestamp\":").append(timestamp);

        if (accountLessonId != null) {
            json.append(",\"accountLessonId\":\"").append(accountLessonId).append("\"");
        }
        if (type != null) {
            json.append(",\"type\":\"").append(type).append("\"");
        }
        if (code != null) {
            json.append(",\"code\":\"").append(code).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Tag: %s, Robot: %s, Time: %d)",
                level, message, tag, robotId,  timestamp);
    }
}
