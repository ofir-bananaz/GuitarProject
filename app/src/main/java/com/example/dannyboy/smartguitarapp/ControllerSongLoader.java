package com.example.dannyboy.smartguitarapp;

import android.app.Activity;
import android.util.Log;

public class ControllerSongLoader {


    private Activity mainActivity;
    private String IP;
    private String port;
    private String guitarControllerCommand;

    public ControllerSongLoader(Activity mainActivity, String IP, String port, String guitarControllerCommand) {
        this.mainActivity = mainActivity;
        this.IP = IP;
        this.port = port;
        this.guitarControllerCommand = guitarControllerCommand;
    }

    public String sendToGuitar(){
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
