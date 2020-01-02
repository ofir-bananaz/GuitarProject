package com.example.dannyboy.smartguitarapp;

import android.app.Activity;
import android.util.Log;


public class ControllerSongLoader {

    private Activity mainActivity;
    private static Integer instancesNum = 0;

    public ControllerSongLoader(Activity mainActivity) {
        instancesNum += 1;
        if (instancesNum > 1) {
            throw new RuntimeException("This calss should be used as singleton");
        }
        this.mainActivity = mainActivity;
    }

    public String load(String guitarControllerCommand, String IP, String port){
        DebugLog.d("myFilter", "Sending data to controller...");

        String serverAnswer = "null";
        try{
            return (new SendDataToControllerAsyncTask(mainActivity)).execute("UDP", IP, port, guitarControllerCommand).get();
        }catch(Exception e){
            Log.e("myFilter", "Exception in sendDataToControllerAsyncTask");
        }
        return serverAnswer;
    }

}
