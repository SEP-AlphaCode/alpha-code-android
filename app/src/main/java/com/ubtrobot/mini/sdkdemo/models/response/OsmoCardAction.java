package com.ubtrobot.mini.sdkdemo.models.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OsmoCardAction {
    /**
     * loop, action, dance, etc.
     */
    private String type;
    private String code;
    private Color color;
    private Integer times;
    private List<OsmoCardAction> actions;

    // Default constructor
    public OsmoCardAction() {
    }

    // Constructor for single action
    public OsmoCardAction(String type, String code, Color color) {
        this.type = type;
        this.code = code;
        this.color = color;
    }

    // Constructor for loop action
    public OsmoCardAction(String type, Integer times, List<OsmoCardAction> actions) {
        this.type = type;
        this.times = times;
        this.actions = actions;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public List<OsmoCardAction> getActions() {
        return actions;
    }

    public void setActions(List<OsmoCardAction> actions) {
        this.actions = actions;
    }

    // Helper methods
    public boolean isLoop() {
        return "loop".equals(type) && times != null && actions != null;
    }

    public boolean isSingleAction() {
        return color != null && !isLoop();
    }

    // Color inner class
    public static class Color {
        private int a;
        private int r;
        private int g;
        private int b;

        public Color() {
        }

        public Color(int a, int r, int g, int b) {
            this.a = a;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        // Getters and setters for Color
        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    public static List<OsmoCardAction> parseActionsArray(JSONArray actionsArray) throws JSONException {
        List<OsmoCardAction> actions = new ArrayList<>();

        for (int i = 0; i < actionsArray.length(); i++) {
            JSONObject actionJson = actionsArray.getJSONObject(i);
            actions.add(parseAction(actionJson));
        }

        return actions;
    }

    private static OsmoCardAction parseAction(JSONObject actionJson) throws JSONException {
        String type = actionJson.getString("type");

        // Check if it's a loop action
        if (actionJson.has("times") && actionJson.has("actions")) {
            int times = actionJson.getInt("times");
            JSONArray nestedActionsArray = actionJson.getJSONArray("actions");
            List<OsmoCardAction> nestedActions = parseActionsArray(nestedActionsArray);

            return new OsmoCardAction(type, times, nestedActions);
        }
        // It's a single action
        else {
            String code = actionJson.getString("code");
            JSONObject colorJson = actionJson.getJSONObject("color");
            OsmoCardAction.Color color = parseColor(colorJson);

            return new OsmoCardAction(type, code, color);
        }
    }

    private static OsmoCardAction.Color parseColor(JSONObject colorJson) throws JSONException {
        OsmoCardAction.Color color = new OsmoCardAction.Color();

        if (colorJson.has("r")) color.setR(colorJson.getInt("r"));
        if (colorJson.has("g")) color.setG(colorJson.getInt("g"));
        if (colorJson.has("b")) color.setB(colorJson.getInt("b"));
        if (colorJson.has("a")) color.setA(colorJson.getInt("a"));

        return color;
    }
}