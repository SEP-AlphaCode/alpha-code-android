package com.ubtrobot.mini.sdkdemo.models.response;

import java.util.List;

public class NLPResponse {
    public static class DataContainer{
        private String code;
        private String text;
        private List<Action> actions;

        public List<Action> getActions() {
            return actions;
        }
        public String getText() {
            return text;
        }
        public String getCode() {
            return code;
        }

    }

    public static class Action {
        private String name;
        private int step;

        public Action() {}

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public int getStep() {
            return step;
        }
        public void setStep(int step) {
            this.step = step;
        }
    }

    private String type;
    private DataContainer data;
    private String lang;

    public String getLang() {
        return lang;
    }

    public String getType() {
        return type;
    }

    public DataContainer getData() {
        return data;
    }
    public NLPResponse(String type, DataContainer data, String lang){
        this.type = type;
        this.data = data;
        this.lang = lang;
    }
}
