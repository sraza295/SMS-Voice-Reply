package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
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

public class MyBackgroundService extends Service implements TextToSpeech.OnInitListener, AIListener
{
    TextToSpeech textToSpeech;
    AIService aiService;
    TextView queryTextView;
    String resultTextView="";
    Button btn;
    String TAG = "hi";
    int countSpeak = 0;
    private boolean isFinalMessage=false;
    String SENT = "SMS_SENT";
    String senderPhoneNo;
    Handler mainHandler;
    Result result;

    BroadcastReceiver sendBroadcastReceiver;

    @Override
    public void onCreate()
    {
        mainHandler = new Handler();
        sendBroadcastReceiver = new SentReceiver();
        //sendSMS("+917979066930", "hello how are you? ");

        final AIConfiguration config = new AIConfiguration("9f9af2962730419db2fda4b0e4ac078e",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        textToSpeech = new TextToSpeech(this, this);
    }

    public MyBackgroundService()
    {
        //Register sms listener
        MessageReceiver.bindListener(MyBackgroundService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void messageReceived(String phoneNo,String contactName, String message)
    {
        //countSpeak=0;
        senderPhoneNo = phoneNo;
        String toSpeak="Meesage from "+contactName+" "+message;
        Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show();
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
            int ttsLang = textToSpeech.setLanguage(new Locale("en", "IN"));
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
        result = response.getResult();

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
        //queryTextView.setText(result.getResolvedQuery());

        // Show results in TextView.
        //resultTextView.setText(result.getFulfillment().getSpeech());


        resultTextView = result.getFulfillment().getSpeech();

        String toSpeak = result.getFulfillment().getSpeech();
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
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        autoStartListening();
                    }
                });

            }
            if (resultTextView.equalsIgnoreCase("Tell your message") || resultTextView.equalsIgnoreCase("tell me your message"))
            {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        isFinalMessage=true;
                        autoStartListening();
                    }
                });
            }
            if (resultTextView.equalsIgnoreCase("Ok. Thank you."))
            {
                return;
            }
        }
    };

    public void sendSMS(String phoneNo, String msg)
    {
        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(MyBackgroundService.this);
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
    }
}
