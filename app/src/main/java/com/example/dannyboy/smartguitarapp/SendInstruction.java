package com.example.dannyboy.smartguitarapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;

import lombok.Builder;

public class SendInstruction extends AsyncTask<String, Void, String>{

	private String TAG = "myFilter";

	@Override
	protected String doInBackground(String... params){
		String protocol = params[0];
		String IP = params[1];
		int port = Integer.parseInt(params[2]);
		String inst = params[3];

		if(protocol.equals("UDP")){
			try{
				DatagramSocket clientSocketSend = new DatagramSocket();
				InetAddress IPAddressServer = InetAddress.getByName(IP);
				byte[] sendData = inst.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
				clientSocketSend.send(sendPacket);
				clientSocketSend.close();
				return "SUCCESS";

			}catch(Exception e){
				DebugLog.d(TAG, "Send instruction failed");
				throw new RuntimeException("Instruction sent has failed: " + e.getMessage());
			}
		}
		throw new RuntimeException("Unsupported protocol");
	}


	@Override
	protected void onPostExecute(String result){
		//Toast.makeText(getBaseContext(), "Message sent...",
		//   Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPreExecute(){
		//  Toast.makeText(getBaseContext(), "Creating socket...",
		//  Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onProgressUpdate(Void... values){
	}
}
