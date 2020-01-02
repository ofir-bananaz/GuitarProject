package com.example.dannyboy.smartguitarapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by dannyboy on 26/12/2017.
 */

class SendDataToControllerAsyncTask extends AsyncTask<String, Void, String>{
	private static final int TIMEOUT = 5*1000;
	private WeakReference<Activity> mWeakActivity;
	private static final String TAG = "myFilter";

	SendDataToControllerAsyncTask(Activity activity){
		mWeakActivity = new WeakReference<Activity>(activity);
	}

	@Override
	protected String doInBackground(String... params){
		String protocol = params[0];
		String IP = params[1];
		int port = Integer.parseInt(params[2]);
		String sentDataStr = params[3];

		try{
			DatagramSocket clientSocketSend = new DatagramSocket();
			InetAddress IPAddressServer = InetAddress.getByName(IP);
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = sentDataStr.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
			clientSocketSend.send(sendPacket);
			clientSocketSend.close();

			return "IDLE";
		}catch(Exception e){
			Log.e(TAG, "Connection AsyncTask Failed");
			DebugLog.d(TAG, Log.getStackTraceString(e));
		}
		return null;
	}


	@Override
	protected void onPostExecute(String result){
	}

	@Override
	protected void onPreExecute(){
	}

	@Override
	protected void onProgressUpdate(Void... values){
	}

}
