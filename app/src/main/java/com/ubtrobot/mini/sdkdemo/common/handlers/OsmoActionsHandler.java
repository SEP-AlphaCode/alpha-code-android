package com.ubtrobot.mini.sdkdemo.common.handlers;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ubtrobot.action.ActionApi;
import com.ubtrobot.action.ActionExApi;
import com.ubtrobot.action.listeners.ActionExListener;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.commons.ResponseListener;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.led.LedApi;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.mini.sdkdemo.log.LogLevel;
import com.ubtrobot.mini.sdkdemo.log.LogManager;
import com.ubtrobot.mini.sdkdemo.models.response.OsmoCardAction;

import java.util.ArrayList;
import java.util.List;

public class OsmoActionsHandler {
    private final ActionApi actionApi = ActionApi.get();
    private final ActionExApi actionExApi = ActionExApi.get();
    private final ExpressApi expApi = ExpressApi.get();
    private final SkillHandler skillHandler = new SkillHandler();
    private static final String TAG = "OsmoHandler";
    private final MouthLedApi ledApi = MouthLedApi.get();

    public interface ExecutionCallback {
        void onCompleted();

        void onError(String error);
    }

    public void executeActions(List<OsmoCardAction> actionCards, ExecutionCallback callback) {
        executeActionList(actionCards, 0, callback);
    }

    private void executeActionList(List<OsmoCardAction> actionCards, int index, ExecutionCallback callback) {
        if (index >= actionCards.size()) {
            callback.onCompleted();
            return;
        }

        OsmoCardAction currentCard = actionCards.get(index);
        executeActionCard(currentCard, new ExecutionCallback() {
            @Override
            public void onCompleted() {
                // Execute next action after current one completes
                executeActionList(actionCards, index + 1, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void executeActionCard(OsmoCardAction card, ExecutionCallback callback) {
        Log.i(TAG, "Executing: " + card);
        if (card.isLoop()) {
            executeLoop(card, callback);
        } else {
            executeSingleAction(card, callback);
        }
    }

    private void executeLoop(OsmoCardAction loopCard, ExecutionCallback callback) {
        int times = loopCard.getTimes();
        List<OsmoCardAction> loopActions = loopCard.getActions();

        executeLoopIteration(loopActions, times, 0, callback);
    }

    private void executeLoopIteration(List<OsmoCardAction> loopActions, int totalTimes, int currentIteration, ExecutionCallback callback) {
        if (currentIteration >= totalTimes) {
            callback.onCompleted();
            return;
        }

        Log.i(TAG, "Loop iteration " + (currentIteration + 1) + "/" + totalTimes);

        executeActionList(loopActions, 0, new ExecutionCallback() {
            @Override
            public void onCompleted() {
                // After completing one iteration, execute the next
                executeLoopIteration(loopActions, totalTimes, currentIteration + 1, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void executeSingleAction(OsmoCardAction actionCard, ExecutionCallback callback) {
        handleActionUnit(actionCard, callback);
    }

    public void stopMouthLed(){
        ledApi.turnOff(Priority.HIGH, null);
    }

    private void handleActionUnit(OsmoCardAction action, ExecutionCallback callback) {
        String type = action.getType();
        String code = action.getCode();
        OsmoCardAction.Color color = action.getColor();
        int colorCode = Color.argb(color.getA(), color.getR(), color.getG(), color.getB());
        ledApi.startNormalModel(colorCode, 9999, Priority.HIGH, null);
        try {
            switch (type) {
                case "action":
                    actionApi.playAction(code, Priority.HIGH, new ResponseListener<Void>() {
                        @Override
                        public void onResponseSuccess(Void unused) {
                            Log.i(TAG, "Action completed: " + code);
                            callback.onCompleted();
                        }

                        @Override
                        public void onFailure(int i, @NonNull String s) {
                            Log.e(TAG, "Action failed: " + s);
                            callback.onError("Action failed: " + s);
                        }
                    });
                    break;

                case "extended_action":
                    executeExtendedAction(code, callback);
                    break;

                case "expression":
                    expApi.doExpress(code);
                    // Assuming expressions are fire-and-forget, complete immediately
                    Log.i(TAG, "Expression executed: " + code);
                    callback.onCompleted();
                    break;

                case "skill_helper":
                    skillHandler.handleSkillHelper(code);
                    // Assuming skill helpers are fire-and-forget, complete immediately
                    Log.i(TAG, "Skill helper executed: " + code);
                    callback.onCompleted();
                    break;

                default:
                    Log.w(TAG, "Unknown action type: " + type);
                    callback.onCompleted(); // Skip unknown types
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing action: " + type + " with code: " + code, e);
            callback.onError("Exception: " + e.getMessage());
        }
    }

    private void executeExtendedAction(String name, ExecutionCallback callback) {
        ActionExListener listener = new ActionExListener() {
            @Override
            public void onActionCompleted() {
                Log.i(TAG, "Extended action " + name + " done!");
                LogManager.log(LogLevel.INFO, TAG, "Extended action " + name + " done!");
                callback.onCompleted();
            }

            @Override
            public void onActonStarted() {
                Log.i(TAG, "Extended action " + name + " started!");
            }

            @Override
            public void onActionProgress(int progress, int total) {
                Log.d(TAG, "Extended action " + name + " progress: " + progress + "/" + total);
            }

            @Override
            public void onActionFailure(int errorCode, @NonNull String errorMsg) {
                Log.e(TAG, "Extended action " + name + " failed: " + errorMsg + " (code: " + errorCode + ")");
                LogManager.log(LogLevel.ERROR, TAG, "Extended action " + name + " failed: " + errorMsg);
                callback.onError("Extended action failed: " + errorMsg);
            }
        };

        try {
            switch (name) {
                case "walk_forward":
                    actionExApi.walkForward(1, Priority.HIGH, listener);
                    break;
                case "walk_backward":
                    actionExApi.walkBackward(1, Priority.HIGH, listener);
                    break;
                case "turn_left":
                    actionExApi.turnLeft(1, Priority.HIGH, listener);
                    break;
                case "turn_right":
                    actionExApi.turnRight(1, Priority.HIGH, listener);
                    break;
                case "make_bows":
                    actionExApi.makeBows(1, Priority.HIGH, listener);
                    break;
                case "make_nods":
                    actionExApi.makeNods(1, Priority.HIGH, listener);
                    break;
                case "shake_heads":
                    actionExApi.shakeHeads(1, Priority.HIGH, listener);
                    break;
                case "slating_heads":
                    actionExApi.slantingHeads(1, Priority.HIGH, listener);
                    break;
                case "shake_hands":
                    actionExApi.shakeHands(1, Priority.HIGH, listener);
                    break;
                case "wave_hands":
                    actionExApi.waveHands(1, Priority.HIGH, listener);
                    break;
                case "make_press_ups":
                    actionExApi.makePressUps(1, Priority.HIGH, listener);
                    break;
                default:
                    Log.w(TAG, "Unknown extended action: " + name);
                    callback.onCompleted(); // Skip unknown actions
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing extended action: " + name, e);
            callback.onError("Exception in extended action: " + e.getMessage());
        }
    }
}