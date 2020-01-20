package com.example.dannyboy.smartguitarapp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static android.content.ContentValues.TAG;

class UdpServerThread extends Thread{

    private int serverPort;
    private DatagramSocket socket;
    MainActivity mainActivity;
    private boolean running;

    UdpServerThread(int serverPort, MainActivity mainActivity) {
        super();
        this.serverPort = serverPort;
        this.mainActivity = mainActivity;
    }

    void setRunningToFalse(){
        running = false;
    }

    @Override
    public void run() {

        this.running = true;
        try {
            mainActivity.updateState("Starting UDP Server");
            if(socket != null) {
                socket.close();
            }
            socket = new DatagramSocket(serverPort);
            socket.setSoTimeout(10000);

            mainActivity.updateState("Press verify to start connection with the controller...");
            Log.e(TAG, "UDP Server is running");

            while(running){
                byte[] buf = new byte[256];
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet); //this code block the program flow
                    mainActivity.updateState(new String(packet.getData(), 0, packet.getLength()));
                }
                catch (SocketTimeoutException e) {
                    mainActivity.updateState("Waiting to for Guitar-Controller connection...\nPress verify to test the connection");
                    mainActivity.initiateVerifyButton();
                }
            }

            Log.e(TAG, "UDP Server ended");
            mainActivity.updateState("Connection to the Guitar-Controller ended...");

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                Log.e(TAG, "socket.close()");
            }
        }
    }
}
