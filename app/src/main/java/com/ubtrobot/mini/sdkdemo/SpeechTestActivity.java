package com.ubtrobot.mini.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.ubtrobot.mini.sdkdemo.apis.STTApi;
import com.ubtrobot.mini.sdkdemo.common.CommandHandler;
import com.ubtrobot.mini.sdkdemo.models.requests.NLPRequest;
import com.ubtrobot.mini.sdkdemo.models.response.NLPResponse;
import com.ubtrobot.mini.sdkdemo.network.ApiClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpeechTestActivity extends Activity {
    STTApi sttApi = ApiClient.getPythonInstance().create(STTApi.class);
    CommandHandler commandHandler = new CommandHandler();
    private static final String TAG = "SpeechTestActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_test_activity);
        EditText editText = findViewById(R.id.input_text);
        Button en = findViewById(R.id.do_speech);
        en.setOnClickListener(v -> {
            String text = editText.getText().toString();
            if(!text.isEmpty()){
                doSpeech(text);
            }
        });
    }
    private void doSpeech(String text) {
        sttApi.processText(new NLPRequest(text)).enqueue(new Callback<NLPResponse>() {
            @Override
            public void onResponse(Call<NLPResponse> call, Response<NLPResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        NLPResponse nlpResponse = response.body();
                        String type = nlpResponse.getType();
                        String lang = nlpResponse.getLang();
                        NLPResponse.DataContainer data = nlpResponse.getData();
                        // Convert DataContainer -> JSON string
                        String jsonString = new Gson().toJson(data);

                        // Convert JSON string -> JSONObject
                        JSONObject jsonData = new JSONObject(jsonString);
                        commandHandler.handleCommand(type, lang, jsonData);

                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<NLPResponse> call, Throwable t) {
                Log.e(TAG, "Response failure: " + t);
            }
        });
    }
}
