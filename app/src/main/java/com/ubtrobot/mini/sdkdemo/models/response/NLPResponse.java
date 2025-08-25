package com.ubtrobot.mini.sdkdemo.models.response;

public class NLPResponse {
    public static class WavContainer {
        private String file_name;
        private String url;
        private double duration;
        private String voice;
        private int text_length;

        public String getFileName() {
            return file_name;
        }

        public String getUrl() {
            return url;
        }

        public double getDuration() {
            return duration;
        }

        public String getVoice() {
            return voice;
        }

        public int getTextLength() {
            return text_length;
        }

        public WavContainer(String file_name, String url, double duration, String voice, int text_length) {
            this.file_name = file_name;
            this.url = url;
            this.duration = duration;
            this.voice = voice;
            this.text_length = text_length;
        }
    }

    public static class DataContainer{
        private String text;
        private WavContainer wav;

        public String getText() {
            return text;
        }

        public WavContainer getWav() {
            return wav;
        }

        public DataContainer(String text, WavContainer wav){
            this.text = text;
            this.wav = wav;
        }

        public DataContainer(String text){
            this.text = text;
            this.wav = null;
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
