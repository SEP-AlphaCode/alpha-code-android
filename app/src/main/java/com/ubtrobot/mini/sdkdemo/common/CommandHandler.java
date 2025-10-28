package com.ubtrobot.mini.sdkdemo.common;

import android.util.Log;

import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.skill.SkillApi;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.action.ActionExApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.express.ExpressApi;
import com.ubtrobot.lib.mouthledapi.MouthLedApi;
import com.ubtrobot.mini.sdkdemo.common.handlers.ActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.DanceHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExpressionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.ExtendedActionHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SkillHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.SystemHandler;
import com.ubtrobot.mini.sdkdemo.common.handlers.TTSHandler;
import com.ubtrobot.sys.SysApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CommandHandler {
    private static final String TAG = "CommandHandler";
    private static boolean allowPlayAction = true;

    public static boolean isAllowPlayAction() {
        return allowPlayAction;
    }

    public static void notifyCleanUpDone() {
        Log.i(TAG, "Can play action again now");
        allowPlayAction = true;
        ActionApi.get().playAction("stand_up", null);
    }

    // Handler instances
    private ActionHandler actionHandler;
    private ExtendedActionHandler extendedActionHandler;
    private SkillHandler skillHandler;
    private ExpressionHandler expressionHandler;
    private DanceHandler danceHandler;
    private TTSHandler ttsHandler;
    private SystemHandler systemHandler;

    public CommandHandler() {
        // Initialize all handlers
        this.actionHandler = new ActionHandler();
        this.extendedActionHandler = new ExtendedActionHandler();
        this.skillHandler = new SkillHandler();
        this.expressionHandler = new ExpressionHandler();
        this.danceHandler = new DanceHandler();
        this.ttsHandler = new TTSHandler();
        this.systemHandler = SystemHandler.get();
    }

    // Method to set socket manager for handlers that need it
    public void setSocketManager(com.ubtrobot.mini.sdkdemo.socket.RobotSocketManager socketManager) {
        this.systemHandler.setSocketManager(socketManager);
    }

    public void handleCommand(String type, JSONObject data, String lang) throws JSONException {
        String text = data.optString("text");
        String code = data.optString("code");

        try {
            switch (type) {
                case "get_system_info":
                    systemHandler.sendRobotStatus();
                    break;
                case "dance_with_music":
                    danceHandler.handleDanceWithMusic(data);
                    break;

                case "skill_helper":
                    skillHandler.handleSkillHelper(code);
                    break;

                case "action":
                    actionHandler.handleAction(code);
                    break;

                case "expression":
                    expressionHandler.handleExpression(code);
                    break;
                case "extended_action":
                    extendedActionHandler.handleExtendedAction(data);
                    break;
                case "stop_all_actions":
                    if (!CommandHandler.isAllowPlayAction()) {
                        Log.i(TAG, "Playing action isn't allowed right now");
                        if (ActionApi.get().isPlaying()) {
                            ActionApi.get().stopAction();

                        }
                        return;
                    }
                    allowPlayAction = false;
                    break;
                case "shut_down":
                    SysApi.get().shutdown();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling command: " + type + ", data: " + data, e);
        }
    }

    private void resetEverything(){
        ActionApi actionApi = ActionApi.get();
        ActionExApi actionExApi = ActionExApi.get();
        MouthLedApi ledApi = MouthLedApi.get();
        ExpressApi expressApi = ExpressApi.get();

        if(actionApi.isPlaying()){
            actionApi.stopAction();
            actionApi.playAction("stand_up", null);
        }
        ledApi.turnOff(Priority.HIGH, null);
        ttsHandler.stopIfPlaying();
        expressApi.stopExpress();
    }
}
