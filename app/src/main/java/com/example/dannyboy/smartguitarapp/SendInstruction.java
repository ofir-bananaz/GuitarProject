package com.example.dannyboy.smartguitarapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class SendInstruction extends AsyncTask<String, Void, String>{

	private String TAG = "myFilter";
	private static final int TIMEOUT = 1;
	private int play_port = 5086;
	private int stop_port = 5087;

	@Override
	protected String doInBackground(String... params){
		String answerFromServer = "NULL";
		String protocol = params[0];
		String IP = params[1];
		int port = Integer.parseInt(params[2]);
		String inst = params[3];


		StringBuilder sentData = new StringBuilder("");
		try{
			if(protocol.equals("TCP")){
				//TODO: IMPLEMENT TCP, EXAMPLE CODE:
//				String fileName = "simple_ours.txt";
//
//
//
//				/*
//				Initiate a TCP connection with "controller".
//				*/
//				DataOutputStream outToServer;
//				BufferedReader inFromServer;
//				Socket clientSocket;
//				try{
//					clientSocket = new Socket(IP, port);
//					outToServer = new DataOutputStream(clientSocket.getOutputStream());
//					inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//				}catch(Exception e){
//					Log.e(TAG, "Could not find server IP:" + IP + "! Is it running?");
//					throw e;
//				}
//
//
//
//				/*
//				Send "binary" data to "controller".
//				*/
//
//				Log.v(TAG, "Sending to server: " + sentDataStr);
//				outToServer.writeBytes(sentDataStr);
//
//
//				//Wait for answer.
//				answerFromServer = inFromServer.readLine();
//
//
//				clientSocket.close();
				DebugLog.e(TAG, "TCP COMS NOT AVAILABLE YET");
				return "FAIL";


			}else if(protocol.equals("UDP")){
				if((inst.equals("PAUSE")||inst.equals("PLAY"))){
					DatagramSocket clientSocketSend = new DatagramSocket();
					InetAddress IPAddressServer = InetAddress.getByName(IP);
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					sendData = inst.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
					clientSocketSend.send(sendPacket);
					clientSocketSend.close();
					//TODO: Wait server response and resend after timeout. example code:
					//				DatagramSocket clientSocketRecv = null;
					//				DatagramPacket receivePacket = null;
					//				String serverResponse="";
					//				try{
					//					clientSocketRecv = new DatagramSocket(port+1);
					//					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					//					clientSocketRecv.setSoTimeout(TIMEOUT*1000);
					//
					//				}catch(Exception e){
					//					Log.e(TAG, "Couldn't listen on port " + port);
					//					DebugLog.d(TAG, Log.getStackTraceString(e));
					//				}
					//				try{
					//					clientSocketRecv.receive(receivePacket);
					//					answerFromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());//Added packet length, else answerFromServer will contain garbage
					//					serverResponse=answerFromServer;
					//				}catch(Exception e){
					//					DebugLog.d(TAG, "No answer from server after "+ TIMEOUT +" seconds, closing.");
					//					serverResponse="RESPONSE_TIME_TIMEOUT";
					//				}
					//				clientSocketRecv.close();
					//				return serverResponse;
				}else if(inst.equals("STOP")){
					DatagramSocket clientSocketSend = new DatagramSocket();
					InetAddress IPAddressServer = InetAddress.getByName(IP);
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					String sentence = inst;
					sendData = sentence.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddressServer, port);
					clientSocketSend.send(sendPacket);
					clientSocketSend.close();

				}




			}
//			DebugLog.d(TAG, "Server answer: " + answerFromServer);
			return "SUCCESS";

		}catch(Exception e){

			DebugLog.d(TAG, "Send instruction failed");

		}


		return "EXCEPTION";
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
