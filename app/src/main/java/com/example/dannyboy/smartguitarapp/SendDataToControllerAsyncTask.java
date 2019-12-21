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
	WeakReference<Activity> mWeakActivity;
	private String TAG = "myFilter";

	SendDataToControllerAsyncTask(Activity activity){
		mWeakActivity = new WeakReference<Activity>(activity);
	}

	String convertStreamToString(java.io.InputStream is){
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	@Override
	protected String doInBackground(String... params){
		String answerFromServer = "NULL";
		String protocol = params[0];
		String IP = params[1];
		int port = Integer.parseInt(params[2]);
		String sentDataStr = params[3];
		int idle_port = 5085;

		StringBuilder sentData = new StringBuilder("");
		try{
			if(protocol.equals("TCP")){
				String fileName = "simple_ours.txt";



                    /*
					Initiate a TCP connection with "controller".
                    */
				DataOutputStream outToServer;
				BufferedReader inFromServer;
				Socket clientSocket;
				try{
					clientSocket = new Socket(IP, port);
					outToServer = new DataOutputStream(clientSocket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				}catch(Exception e){
					Log.e(TAG, "Could not find server IP:" + IP + "! Is it running?");
					throw e;
				}



                    /*
                    Send "binary" data to "controller".
                    */

				Log.v(TAG, "Sending to server: " + sentDataStr);
				outToServer.writeBytes(sentDataStr);


				//Wait for answer.
				answerFromServer = inFromServer.readLine();


				clientSocket.close();


			}else if(protocol.equals("UDP")){

				DatagramSocket clientSocketSend = new DatagramSocket();
				InetAddress IPAddressServer = InetAddress.getByName(IP);
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				String sentence = sentDataStr;
				sendData = sentence.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
				clientSocketSend.send(sendPacket);
				clientSocketSend.close();

				//Wait for song to finish (i.e., wait for controller to be in IDLE state)
//				DatagramSocket clientSocketRecv = null;
//				DatagramPacket receivePacket = null;
				String serverResponse = "";
//				try{
//					clientSocketRecv = new DatagramSocket(idle_port);
//					receivePacket = new DatagramPacket(receiveData, receiveData.length);
//					clientSocketRecv.setSoTimeout(TIMEOUT);
//
//				}catch(Exception e){
//					Log.e(TAG, "Couldn't listen on port " + idle_port);
//					DebugLog.d(TAG, Log.getStackTraceString(e));
//				}
//				try{
//					clientSocketRecv.receive(receivePacket);
//					answerFromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());//Added packet length, else answerFromServer will contain garbage
//					serverResponse = answerFromServer;
//				}catch(Exception e){
//					//					DebugLog.d(TAG, "No answer from server after "+ TIMEOUT +" seconds, closing.");
//					serverResponse = "RESPONSE_TIME_TIMEOUT";
//
//				}
				//For testing:
				serverResponse = "IDLE";
//				clientSocketRecv.close();
				return serverResponse;


			}



		}catch(Exception e){
			Log.e(TAG, "Connection AsyncTask Failed");
			DebugLog.d(TAG, Log.getStackTraceString(e));

		}


		return null;
	}


	@Override
	protected void onPostExecute(String result){
//		DebugLog.d(TAG, "Server answer: " + result);


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
