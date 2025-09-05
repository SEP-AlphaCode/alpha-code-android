package com.ubtrobot.mini.sdkdemo.common.handlers;

import org.json.JSONObject;

import com.ubtrobot.mini.sdkdemo.DanceWithMusicActivity;

public class DanceHandler {
    private static final String TAG = "DanceHandler";
    private DanceWithMusicActivity danceWithMusicActivity;

    public DanceHandler() {
        this.danceWithMusicActivity = DanceWithMusicActivity.get();
    }

    public void handleDanceWithMusic(JSONObject data) {
        if (danceWithMusicActivity != null) {
            danceWithMusicActivity.JumpWithMusic(data);
        }
    }
}
