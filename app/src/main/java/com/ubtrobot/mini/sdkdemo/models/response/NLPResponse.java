package com.ubtrobot.mini.sdkdemo.models.response;

public class NLPResponse {
    public class DataContainer{
        private String text;

        public String getText() {
            return text;
        }
        public DataContainer(String text){
            this.text = text;
        }
    }
    private String type;
    private DataContainer data;

    public String getType() {
        return type;
    }

    public DataContainer getData() {
        return data;
    }
    public NLPResponse(String type, DataContainer data){
        this.type = type;
        this.data = data;
    }
}
