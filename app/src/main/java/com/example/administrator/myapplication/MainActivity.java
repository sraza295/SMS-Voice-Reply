package com.example.administrator.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity
{
    /*TextToSpeech textToSpeech;
    AIService aiService;
    TextView queryTextView, resultTextView;
    Button btn;
    String TAG = "hi";
    int countSpeak = 0;
    private boolean isFinalMessage=false;
    String SENT = "SMS_SENT";
    String senderPhoneNo;
    private static final int PERMISSION_REQUEST_CODE = 1;

    BroadcastReceiver sendBroadcastReceiver;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*sendBroadcastReceiver = new SentReceiver();

        //sendSMS("+917979066930", "hello how are you? ");

        queryTextView = (TextView) findViewById(R.id.textView);
        resultTextView = (TextView) findViewById(R.id.textView1);

        final AIConfiguration config = new AIConfiguration("9f9af2962730419db2fda4b0e4ac078e",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        //initialisation();
        textToSpeech = new TextToSpeech(MainActivity.this, this);

        MessageReceiver.bindListener1(MainActivity.this);*/
        startService(new Intent(this, MyBackgroundService.class));
    }

    /*public void initialisation()
    {
        textToSpeech = new TextToSpeech(MainActivity.this, this);
    }

    public void messageReceived(String phoneNo,String message)
    {
        //initialisation();
        senderPhoneNo = phoneNo;
        String toSpeak="Meesage from "+phoneNo+" "+message;
        listenResponse(toSpeak);
    }

    private void listenResponse(String toSpeak)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");

        Log.i("TTS", "button clicked: " + toSpeak);
        int speechStatus = textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, map);
        countSpeak++;

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS) {
            int ttsLang = textToSpeech.setLanguage(Locale.US);
            textToSpeech.setOnUtteranceProgressListener(mProgressListener);
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language is not supported!");
            } else {
                Log.i("TTS", "Language Supported.");
            }
            Log.i("TTS", "Initialization success.");
        } else {
            Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        if (isFinalMessage)
        {
            String phoneNo = senderPhoneNo;
            String msg = result.getResolvedQuery();
            sendSMS(phoneNo, msg);
            countSpeak=0;
            return;

        }

        // Show query in TextView.
        queryTextView.setText(result.getResolvedQuery());

        // Show results in TextView.
        resultTextView.setText(result.getFulfillment().getSpeech());

        String toSpeak = resultTextView.getText().toString();
        listenResponse(toSpeak);

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {
    }

    public void autoStartListening() {
        aiService.startListening();
    }

    private abstract class runnable implements Runnable {
    }

    private UtteranceProgressListener mProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
        } // Do nothing

        @Override
        public void onError(String utteranceId) {
        } // Do nothing.

        @Override
        public void onDone(String utteranceId) {
            System.out.println("inside uuter");
            if (countSpeak == 1) {
                listenResponse("would you like to give reply?");

            }
            if (countSpeak == 2)
            {
                MainActivity.this.runOnUiThread(new runnable() {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            autoStartListening();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (resultTextView.getText().toString().equalsIgnoreCase("Tell your message") || resultTextView.getText().toString().equalsIgnoreCase("tell me your message")) {
                MainActivity.this.runOnUiThread(new runnable() {
                    public void run() {
                        isFinalMessage=true;
                        autoStartListening();
                        //sendSMS("+917979066930", queryTextView.getText().toString());
                    }
                });
            }
            if (resultTextView.getText().toString().equalsIgnoreCase("Ok. Thank you.") || resultTextView.getText().toString().equalsIgnoreCase("tell me your message")) {
                return;
            }
        }
    };

    public void sendSMS(String phoneNo, String msg)
    {
        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(MainActivity.this);
        final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager
                .getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);

        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));

        int smsToSendFrom = simCardList.get(1); //assign your desired sim to send sms, or user selected choice
        SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom)
                .sendTextMessage(phoneNo, null, msg, sentPI, null); //use your phone number, message and pending intents
    }

    class SentReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "sms_sent", Toast.LENGTH_SHORT).show();
                    listenResponse("Your Message is sent");
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                    listenResponse("Your Message is not sent");
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                    listenResponse("Your network is not available");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }*/

}
