package com.example.dannyboy.smartguitarapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnDoneListener{
	public final static String EXTRA_MESSAGE = "com.example.slimpc.lab_test2";
	private static final String TCP_IP = "127.0.0.1";
	private static final int TCP_port = 10000;
	private static final String UDP_IP = "192.168.2.100";
	private static final int UDP_port = 10080; //Port 80 cannot be used in Linux
	private static String songsFolderName = "SmartGuitar";
	public final static String songsFolderAbsolutePath = Environment.getExternalStorageDirectory() + "/" + songsFolderName;
	String TAG = "myFilter";
	Song lastSelectedSong = null;
	Song lastSentSong = null;
	ArrayList<Song> songArrayList = new ArrayList<>();
	private Button send_button, stop_button;
	private ToggleButton playPauseButton;
	private CheckBox interactive_modeCheckBox;
	private EditText ipEditText;
	private EditText portEditText;
	private EditText tempoEditText;
	private TextView resTextView;
	private TextView debugView;
	private Spinner spinner;
	//Must be declared here:
	private String fileURL = "undefined";
	private String fileName = "";//tab file name to be parsed!!
	private String binString = "nullDataString";
	private DebugLog debugLog;
	private PromptDialog promptDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		interactive_modeCheckBox = findViewById(R.id.checkBox);
		send_button = findViewById(R.id.send_button);
		playPauseButton = findViewById(R.id.playPauseButton);
		stop_button = findViewById(R.id.stop_button);
		ipEditText = findViewById(R.id.ipEditText);
		portEditText = findViewById(R.id.portEditText);
		tempoEditText = findViewById(R.id.tempoEditText);
		//		resTextView = findViewById(R.id.txtResult);
		debugView = findViewById(R.id.debugView);
		debugView.setMaxLines(Integer.MAX_VALUE);
		spinner = findViewById(R.id.spinner);
		debugView.setMovementMethod(new ScrollingMovementMethod());
		debugLog = new DebugLog(true, debugView);
		promptDialog = new PromptDialog(MainActivity.this);
		initiateSongsList();
		//		fillSpinner();
		send_button.setEnabled(false);
		playPauseButton.setEnabled(false);
		stop_button.setEnabled(false);
		interactive_modeCheckBox.setChecked(false);
		interactive_modeCheckBox.setEnabled(true);


		// Create an ArrayAdapter using the array list and a default spinner layout
		ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, android.R.layout.simple_spinner_dropdown_item, songArrayList);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){

				try{
					lastSelectedSong = (Song) parentView.getSelectedItem();//Should check somehow if song is already in the device.
					if(!lastSelectedSong.getName().contains("<Select Song>")){
						DebugLog.d(TAG, "User selected: " + lastSelectedSong.getName());
						send_button.setEnabled(true);
						playPauseButton.setEnabled(true);
						if(lastSelectedSong.getName().contains("Download new song from URL")){
							DownloadFileAsyncTask downloadFileAsyncTask;
							downloadFileAsyncTask = new DownloadFileAsyncTask(MainActivity.this);
							downloadFileAsyncTask.setOnDoneListener(MainActivity.this);
							promptDialog.promptUser(downloadFileAsyncTask);
						}
					}

				}catch(Exception e){
					Log.d(TAG, "Spinner initiation failed!");
					e.printStackTrace();
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView){
				Log.d(TAG, "User canceled selection");//<--Doesn't show on cancellation for some reason

				Toast.makeText(getApplicationContext(), "User canceled selection...", Toast.LENGTH_LONG).show();

			}

		});
		send_button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				String serverAnswer = "null";
				String controllerIP;
				String controllerPort;
				int tempo;
				if(ipEditText.getText().toString().matches("")){
					controllerIP = ipEditText.getHint().toString();//"192.168.2.100";
				}else{
					controllerIP = ipEditText.getText().toString();
				}
				if(portEditText.getText().toString().matches("")){
					controllerPort = portEditText.getHint().toString();//"10080";
				}else{
					controllerPort = portEditText.getText().toString();
				}
				if(tempoEditText.getText().toString().matches("")){
					tempo = Integer.parseInt( tempoEditText.getHint().toString());
				}else{
					tempo = Integer.parseInt( tempoEditText.getText().toString());
				}

				MediaPlayer error = MediaPlayer.create(MainActivity.this, R.raw.error);
				MediaPlayer success = MediaPlayer.create(MainActivity.this, R.raw.success);
//				if(!lastSelectedSong.isInDevice()){
//					DebugLog.e(TAG, "Invalid song selection");
//					return;
//				}


				try{

					serverAnswer = (new Parser(lastSelectedSong,tempo)).sendToGuitar(MainActivity.this, controllerIP, controllerPort, interactive_modeCheckBox.isChecked());
				}catch(Exception e){
					//Log.e(TAG, "Error: could not generate bitString for file name " + fileName);
					//					DebugLog.e(TAG, "Error: exception while trying to send data" + fileName,e);
					error.start();
					return;
				}


				if(serverAnswer.contains("Error: Both strings are NOT equal!")){
					//					resTextView.setTextColor(Color.RED);
					DebugLog.d(TAG, "Failure: Both strings are NOT equal!");
					error.start();
				}else if(serverAnswer.contains("RESPONSE_TIME_TIMEOUT")){
					DebugLog.d(TAG, "Failure: Server response timeout");
					return;
				}else{
					//					resTextView.setTextColor(Color.GREEN);
					DebugLog.d(TAG, "Success: both strings are identical!");
					success.start();
				}


			}
		});

		playPauseButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				String controllerIP;
				String controllerPort;
				int tempo;
				String answer;
				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				if(wifi.isWifiEnabled() == false){
					DebugLog.d(TAG, "No Wi-Fi connection!");
					return;
				}
				if(ipEditText.getText().toString().matches("")){
					controllerIP = ipEditText.getHint().toString();//"192.168.2.101";
				}else{
					controllerIP = ipEditText.getText().toString();

				}
				if(portEditText.getText().toString().matches("")){
					controllerPort = portEditText.getHint().toString();//"10080";
				}else{
					controllerPort = portEditText.getText().toString();
				}
				if(tempoEditText.getText().toString().matches("")){
					tempo = 256-Integer.parseInt( tempoEditText.getHint().toString());
				}else{
					tempo = 256-Integer.parseInt( tempoEditText.getText().toString());
				}
				if(playPauseButton.isChecked()){

					//TODO: Improve Send to controller
					if(lastSentSong != null && !lastSentSong.equals(lastSelectedSong)){
						lastSentSong.setInGuitar(false);
					}

					if(lastSelectedSong.isInGuitar() == false){
						(new Parser(lastSelectedSong,tempo)).sendToGuitar(MainActivity.this, controllerIP, controllerPort, interactive_modeCheckBox.isChecked());
						// Data sent. Here we assume data is always sent successfully and confirmed by guitar controller
						lastSelectedSong.setInGuitar(true);
						DebugLog.d(TAG, "Song data sent.");
						stop_button.setEnabled(true);

						spinner.setEnabled(false);
						send_button.setEnabled(false);

						//This is a hacky workaround for a new incoming message event (needs to be replaced with a listener)
						new Thread(new Runnable(){
							public void run(){
								// a potentially  time consuming task
								//Wait for song to finish (i.e., wait for controller to be in IDLE state)
								DatagramSocket clientSocketRecv = null;
								DatagramPacket receivePacket = null;
								byte[] receiveData = new byte[1024];
								int TIMEOUT = 60 * 1000;
								int idle_port = 5085;
								String serverResponse = "";
								long startTime = System.currentTimeMillis();
								try{
									clientSocketRecv = new DatagramSocket(idle_port);
									receivePacket = new DatagramPacket(receiveData, receiveData.length);
									clientSocketRecv.setSoTimeout(TIMEOUT);

								}catch(Exception e){
									//									DebugLog.d(TAG, Log.getStackTraceString(e));
									int fix_tries = 1;

									while(fix_tries > 0){
										try{
											clientSocketRecv.close();
											clientSocketRecv = new DatagramSocket(idle_port);
											receivePacket = new DatagramPacket(receiveData, receiveData.length);
											clientSocketRecv.setSoTimeout(TIMEOUT);
										}catch(Exception e1){
											Log.e(TAG, "Couldn't listen on port " + idle_port);
										}
									}


								}
								try{
									clientSocketRecv.receive(receivePacket);
									serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());//Added packet length, else answerFromServer will contain garbage

								}catch(Exception e){
									//					DebugLog.d(TAG, "No answer from server after "+ TIMEOUT +" seconds, closing.");
									serverResponse = "RESPONSE_TIME_TIMEOUT";

								}
								clientSocketRecv.close();

								//For testing (Song ends after TIMEOUT):
								serverResponse = "IDLE";
								long difference = System.currentTimeMillis() - startTime;
								//								if(serverResponse.equals("IDLE")){
								//									DebugLog.d(TAG, "Song finished! (Length: "+difference+")");
								//								}
								stop_button.post(new Runnable(){
									public void run(){
										stop_button.setEnabled(false);
									}
								});
								send_button.post(new Runnable(){
									public void run(){
										send_button.setEnabled(true);
									}
								});
								spinner.post(new Runnable(){
									public void run(){
										spinner.setEnabled(true);
									}
								});
								playPauseButton.post(new Runnable(){
									public void run(){
										playPauseButton.setChecked(false);
									}
								});
								lastSentSong = lastSelectedSong;
								lastSentSong.setInGuitar(false);


							}
						}).start();


						//


					}else{
						(new SendInstruction()).execute("UDP", controllerIP, controllerPort, "PLAY");
						DebugLog.d(TAG, "Play instruction sent.");
						stop_button.setEnabled(true);
						spinner.setEnabled(false);
						send_button.setEnabled(false);
					}

				}else{
					(new SendInstruction()).execute("UDP", controllerIP, controllerPort, "PAUSE");
					DebugLog.d(TAG, "Pause instruction sent.");


				}


			}
		});
		stop_button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				String controllerIP;
				String controllerPort;
				if(ipEditText.getText().toString().matches("")){
					controllerIP = ipEditText.getHint().toString();//"192.168.2.101";
				}else{
					controllerIP = ipEditText.getText().toString();
				}
				if(portEditText.getText().toString().matches("")){
					controllerPort = portEditText.getHint().toString();//"10080";
				}else{
					controllerPort = portEditText.getText().toString();
				}
				lastSentSong = lastSelectedSong;
				lastSentSong.setInGuitar(false);
				//TODO: Improve Send stop to controller
				(new SendInstruction()).execute("UDP", controllerIP, controllerPort, "STOP");
				DebugLog.d(TAG, "Stop instruction sent.");
				stop_button.setEnabled(false);
				send_button.setEnabled(true);

				spinner.setEnabled(true);
				playPauseButton.setChecked(false);

				return;

			}
		});

	}

	public TextView getDebugView(){
		return debugView;
	}

	public void onDone(Object[] result){
		if(result[0].equals(0)){
			Song temp = songArrayList.get(songArrayList.size() - 1);
			songArrayList.remove(songArrayList.size() - 1);
			songArrayList.add((Song) result[1]);
			songArrayList.add(temp);
			DebugLog.d(TAG, "File " + ((Song) result[1]).getName() + " download succeeded!");
			int songIDX = songArrayList.size() - 2;
			spinner.setSelection(songIDX); // doesnt work

		}else if(result[0].equals(1)){
			DebugLog.d(TAG, "File " + ((Song) result[1]).getName() + " already exists!");
		}else{
			DebugLog.d(TAG, "File " + ((Song) result[1]).getName() + " download failed!");
		}
	}

	private void initiateSongsList(){
		File folder = new File(MainActivity.songsFolderAbsolutePath);
		if(!folder.isDirectory()){
			//			Commented line is the same, but containing a symbolic link.
			//			Log.d(TAG,"First time launch, creating song folder; path: "+Environment.getExternalStorageDirectory()+"/"+songsFolderName);
			DebugLog.d(TAG, "First time launch, creating song folder; path: " + "sdcard" + "/" + songsFolderName);
			if(!folder.mkdirs()){
				Log.e(TAG, "Creating song directory failed!");
			}

		}else{
			DebugLog.d(TAG, "Checking for new songs in: " + "sdcard" + "/" + songsFolderName);
			lastSelectedSong = new Song("<Select Song>");
			songArrayList.add(lastSelectedSong);


			File[] listOfFiles = folder.listFiles();

			for(File file : listOfFiles){
				if(file.isFile()){
					DebugLog.d(TAG, "Found: " + file.getName());
					songArrayList.add(new Song(file.getName(), Environment.getExternalStorageDirectory() + "/" + songsFolderName));

				}
			}


		}

		songArrayList.add(new Song("Download new song from URL..."));
	}

}
