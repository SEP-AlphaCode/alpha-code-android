package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtrobot.action.ActionExApi;
import com.ubtrobot.action.listeners.ActionExListener;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExtendedActionHandler {
    private static final String TAG = "ExtendedActionHandler";
    private ActionExApi actionExApi;

    public ExtendedActionHandler() {
        this.actionExApi = ActionExApi.get();
    }

    public void handleExtendedAction(JSONObject data) {
        Log.i(TAG, "handleExtendedAction: " + data);
        LogManager.log(LogLevel.INFO, TAG, "handleExtendedAction: " + data);

        if (data.has("actions")) {
            JSONArray actions = data.optJSONArray("actions");
            if (actions != null) {
                executeActionsSequentially(actions, 0);
            }
        } else {
            // fallback for single action
            String name = data.optString("name");
            int step = data.optInt("step", 1);
            executeAction(name, step, () -> {
                Log.i(TAG, "Single extended action finished");
            });
        }
    }

    private void executeActionsSequentially(JSONArray actions, int index) {
        if (index >= actions.length()) {
            Log.i(TAG, "All extended actions finished");
            return;
        }

        JSONObject actionObj = actions.optJSONObject(index);
        if (actionObj != null) {
            String actName = actionObj.optString("name");
            int actStep = actionObj.optInt("step", 1);

            Log.i(TAG, "Executing action " + actName + " step=" + actStep);
            LogManager.log(LogLevel.INFO, TAG, "Executing action " + actName + " step=" + actStep);

            executeAction(actName, actStep, () -> {
                // After current action is done, execute the next one
                executeActionsSequentially(actions, index + 1);
            });
        } else {
            // If actionObj is null, skip to the next action
            executeActionsSequentially(actions, index + 1);
        }
    }

    private void executeAction(String name, int step, Runnable onComplete) {
        if (name == null || step <= 0) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ActionExListener listener = new ActionExListener() {
            @Override
            public void onActionCompleted() {
                Log.i(TAG, "Extended action " + name + " done!");
                LogManager.log(LogLevel.INFO, TAG, "Extended action " + name + " done!");
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onActonStarted() {

            }

            @Override
            public void onActionProgress(int i, int i1) {

            }

            @Override
            public void onActionFailure(int i, @NonNull String s) {
                Log.e(TAG, "Extended action " + name + " failed: " + s);
                LogManager.log(LogLevel.ERROR, TAG, "Extended action " + name + " failed: " + s);
                if (onComplete != null) onComplete.run(); // still call onComplete on failure
            }
        };

        switch (name) {
            case "walk_forward":
                actionExApi.walkForward(step, Priority.HIGH, listener);
                break;
            case "walk_backward":
                actionExApi.walkBackward(step, Priority.HIGH, listener);
                break;
            case "turn_left":
                actionExApi.turnLeft(step, Priority.HIGH, listener);
                break;
            case "turn_right":
                actionExApi.turnRight(step, Priority.HIGH, listener);
                break;
            case "make_bows":
                actionExApi.makeBows(step, Priority.HIGH, listener);
                break;
            case "make_nods":
                actionExApi.makeNods(step, Priority.HIGH, listener);
                break;
            case "shake_heads":
                actionExApi.shakeHeads(step, Priority.HIGH, listener);
                break;
            case "slating_heads":
                actionExApi.slantingHeads(step, Priority.HIGH, listener);
                break;
            case "shake_hands":
                actionExApi.shakeHands(step, Priority.HIGH, listener);
                break;
            case "wave_hands":
                actionExApi.waveHands(step, Priority.HIGH, listener);
                break;
            case "make_press_ups":
                actionExApi.makePressUps(step, Priority.HIGH, listener);
                break;
            default:
                Log.w(TAG, "Unknown extended action: " + name);
                if (onComplete != null) onComplete.run();
                break;
        }
    }
}
