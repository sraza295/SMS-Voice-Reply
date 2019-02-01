package com.example.administrator.myapplication;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CustomLog
{
    public void logSave(String data)
    {
        File file = new File(Environment.getExternalStorageDirectory()+"/voicereply.log");
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file,true);
            writer.write(data+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}