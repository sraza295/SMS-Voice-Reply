package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

public class MessageReceiver extends BroadcastReceiver
{

    private static MyBackgroundService myBackgroundServiceListener;
    private static MainActivity mainActivity1;
    private String contactName="";

    //create constructor with ActivityObj to get activity from Activity class
    public MessageReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        CustomLog customLog=new CustomLog();

        context.startService(new Intent(context, MyBackgroundService.class));

        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for(int i=0; i<pdus.length; i++)
        {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String originatingAddress =smsMessage.getDisplayOriginatingAddress();
            customLog.logSave("Before Regex originatingAddress : "+originatingAddress);
            //originatingAddress= originatingAddress.replaceAll("[^0-9]", "");
            System.out.println(originatingAddress);

                try {
                    Long num = Long.valueOf(originatingAddress);
                    System.out.println("This is valid a number");
                    customLog.logSave("This is valid a number");
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                    System.out.println("is not a number");
                    customLog.logSave("This is not valid a number");
                    return;
                }

            String contactName = getContactName(originatingAddress, context);
                if(contactName==null)
                    contactName="Unknown Number";
            customLog.logSave("contactName : "+contactName);
            String message=smsMessage.getDisplayMessageBody();
             myBackgroundServiceListener.messageReceived(originatingAddress,contactName,message);
        }
    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }
        return contactName;
    }

    public static void bindListener(MyBackgroundService listener){
        myBackgroundServiceListener = listener;
    }

    /*public static void bindListener1(MainActivity mainActivity) {
        mainActivity1=mainActivity;
    }*/
}
