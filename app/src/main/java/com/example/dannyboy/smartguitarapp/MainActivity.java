package com.example.dannyboy.smartguitarapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnDoneListener {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 1001;
    private static String songsFolderName = "SmartGuitar";
    private final File folder = new File(Environment.getExternalStorageDirectory(), songsFolderName);
	String TAG = "myFilter";
	Song lastSelectedSong = null;
	Song lastSentSong = null;
	ArrayList<Song> songArrayList = new ArrayList<>();
	private Button verifyButton, stopButton, loopButton;
	private ToggleButton playPauseButton;
	private CheckBox interactive_modeCheckBox;
	private EditText ipEditText;
	private EditText portEditText;
	private EditText tempoEditText;
	private EditText loopStartMeasureEditText;
	private EditText loopEndMeasureEditText;
	private TextView debugView;
	private Spinner spinner;
	private Spinner guitarProTracksSpinner;
	private SongTrack selectedTrack;
	private PromptDialog promptDialog;

	private ControllerSongLoader controllerSongLoader = new ControllerSongLoader(MainActivity.this); // Use as a singleton
	private String controllerIP;
	private String controllerPort;
	private int controllerTime;
    private List<SongTrack> lastSelectedSongTracksList = new ArrayList<>();
	private Integer currSongEndMeasure;

	TextView textViewState;
	static final int UdpServerPORT = 4555;
	UdpServerThread udpServerThread;

	private void requestPermissions() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);

            }
        }
    }

	/**
	 * Updates the application parameters by the text field in the application that were filled by the user
	 */
	private void updateVariablesFromTextBoxes() {
		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(!wifi.isWifiEnabled()){
			DebugLog.d(TAG, "No Wi-Fi connection!");
			return;
		}
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
		// The (256-controllerTime) was defined in the project 1st iteration. See how the controller translates this to keep the LEDs on to understand this better.
		if(tempoEditText.getText().toString().matches("")){
			controllerTime = 256-Integer.parseInt( tempoEditText.getHint().toString());
		}else{
			controllerTime = 256-Integer.parseInt( tempoEditText.getText().toString());
			if (controllerTime <= 0) {
				DebugLog.d(TAG, "Tempo cannot be bigger than 256, setting controllerTime to 127");
				controllerTime = 127;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState){
        requestPermissions();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		interactive_modeCheckBox = findViewById(R.id.checkBox);
		verifyButton = findViewById(R.id.sendButton);
		playPauseButton = findViewById(R.id.playPauseButton);
		stopButton = findViewById(R.id.stopButton);
		loopButton = findViewById(R.id.loopButton);
		ipEditText = findViewById(R.id.ipEditText);
		portEditText = findViewById(R.id.portEditText);
		tempoEditText = findViewById(R.id.tempoEditText);
		loopStartMeasureEditText = findViewById(R.id.loopStart);
		loopEndMeasureEditText = findViewById(R.id.loopEnd);
		debugView = findViewById(R.id.debugView);
		debugView.setMaxLines(Integer.MAX_VALUE);
		spinner = findViewById(R.id.spinner);
		guitarProTracksSpinner = findViewById(R.id.tracksSpinner);
		debugView.setMovementMethod(new ScrollingMovementMethod());
		new DebugLog(true, debugView);
		promptDialog = new PromptDialog(MainActivity.this);
		initiateSongsList();
		initiateTracksList();
		verifyButton.setEnabled(true);
		playPauseButton.setEnabled(false);
		stopButton.setEnabled(false);
		resetLoopButton();
		interactive_modeCheckBox.setChecked(false);
		interactive_modeCheckBox.setEnabled(true);

		textViewState = (TextView)findViewById(R.id.state);


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
						verifyButton.setEnabled(false);
						playPauseButton.setEnabled(true);
						if(lastSelectedSong.getName().contains("Download new song from URL")){
							DownloadFileAsyncTask downloadFileAsyncTask;
							downloadFileAsyncTask = new DownloadFileAsyncTask(MainActivity.this);
							downloadFileAsyncTask.setOnDoneListener(MainActivity.this);
							promptDialog.promptUser(downloadFileAsyncTask);
						}
					} else {
						playPauseButton.setEnabled(false);
					}


				}catch(Exception e){
					Log.d(TAG, "Spinner initiation failed!");
					e.printStackTrace();
				}

				updateTracks();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView){
				Log.d(TAG, "User canceled selection");//<--Doesn't show on cancellation for some reason
				Toast.makeText(getApplicationContext(), "User canceled selection...", Toast.LENGTH_LONG).show();
			}

		});

		ArrayAdapter<SongTrack> tracksAdapter = new ArrayAdapter<SongTrack>(this, android.R.layout.simple_spinner_dropdown_item, lastSelectedSongTracksList);
		guitarProTracksSpinner.setAdapter(tracksAdapter);

		guitarProTracksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id){

				try{
					selectedTrack = (SongTrack) parentView.getSelectedItem();//Should check somehow if song is already in the device.
				}catch(Exception e){
					Log.d(TAG, "Spinner initiation failed!");
					e.printStackTrace();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView){
				Log.d(TAG, "User canceled track selection");//<--Doesn't show on cancellation for some reason
				Toast.makeText(getApplicationContext(), "User canceled track selection...", Toast.LENGTH_LONG).show();
			}

		});


		verifyButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				updateVariablesFromTextBoxes();
				ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, "STOP");
			}
		});

		playPauseButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				String answer;
				updateVariablesFromTextBoxes();

				if(playPauseButton.isChecked()){

					if(lastSentSong != null && !lastSentSong.equals(lastSelectedSong)){
						lastSentSong.setInGuitar(false);
					}

					if(!lastSelectedSong.isInGuitar()){
						DebugLog.d(TAG, "Preparing Song data....");
						Toast.makeText(MainActivity.this,
								"Preparing Song data....",
								Toast.LENGTH_LONG)
								.show();

						ParsedSong parsedSong = lastSelectedSong.prepareSongForController(interactive_modeCheckBox.isChecked(), controllerTime, selectedTrack.getIndex());
						DebugLog.d(TAG, "Sending Song data....");
						controllerSongLoader.load(parsedSong.getParsedString(), controllerIP, controllerPort);

						DebugLog.d(TAG, "Song data sent.");
						stopButton.setEnabled(true);
						spinner.setEnabled(false);
						verifyButton.setEnabled(false);
						enableLoopButton(parsedSong.getMeasuresStartlist().size() - 1); // TODO - enable with the parameters from the Guitar pro loop

					}else{
						ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, "PLAY");
						DebugLog.d(TAG, "Play instruction sent.");
						stopButton.setEnabled(true);
						spinner.setEnabled(false);
						verifyButton.setEnabled(false);
					}

				}else{
					ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, "PAUSE");
					DebugLog.d(TAG, "Pause instruction sent.");
				}


			}
		});
		stopButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
			updateVariablesFromTextBoxes();
			lastSentSong = lastSelectedSong;
			lastSentSong.setInGuitar(false);
			ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, "~0/0/0"); //disable loop instruction
			ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, "STOP");
			DebugLog.d(TAG, "Stop instruction sent.");
			stopButton.setEnabled(false);
			verifyButton.setEnabled(true);
			resetLoopButton();
			spinner.setEnabled(true);
			playPauseButton.setChecked(false);
			}
		});


		loopButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				updateVariablesFromTextBoxes();
				Integer startMeasure = Integer.parseInt(loopStartMeasureEditText.getText().toString());
				Integer endMeasure = Integer.parseInt(loopEndMeasureEditText.getText().toString());
				if (startMeasure >= currSongEndMeasure || endMeasure > currSongEndMeasure) {
					Toast.makeText(getApplicationContext(), "Choose Start/End values between song limits (Use value in the hints range)", Toast.LENGTH_LONG).show();
					return;
				}
				if (startMeasure >= endMeasure) {
					String msg = "Illegal values for Measures loop! Set values between 0 to " + currSongEndMeasure + ". Make sure that start < end.";
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
					return;
				}
				ControllerInstruction.create().execute("UDP", controllerIP, controllerPort, prepareLoopInstruction(lastSelectedSong.getEventIndexForMeasure(startMeasure), lastSelectedSong.getEventIndexForMeasure(endMeasure))); // TODO - check with Ohad what is the right command to send
				DebugLog.d(TAG, "Loop instruction sent.");

			}
		});
	}

	@Override
	protected void onResume() {
		udpServerThread = new UdpServerThread(UdpServerPORT, this);
		udpServerThread.start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(udpServerThread != null){
			udpServerThread.setRunningToFalse();
			udpServerThread = null;
		}
		super.onPause();
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


	private ControllerSongParser controllerByFileType(String filePath) {
		if (filePath.contains(".gp")) {
			return com.example.dannyboy.smartguitarapp.GuitarProParser.getInstance();
		} else if (filePath.contains(".txt")) {
			return com.example.dannyboy.smartguitarapp.TextTabParser.getInstance();
		}
		throw new IllegalArgumentException("Could not match a parser to filetype");
	}

	private void initiateSongsList(){
		if(!folder.isDirectory()){
			//			Commented line is the same, but containing a symbolic link.
			//			Log.d(TAG,"First time launch, creating song folder; path: "+Environment.getExternalStorageDirectory()+"/"+songsFolderName);
			DebugLog.d(TAG, "First time launch, creating song folder; path: " + folder.getAbsolutePath());
			if(!folder.mkdirs()){
				Log.e(TAG, "Creating song directory failed!");
			}

		}else {
            DebugLog.d(TAG, "Checking for new songs in: " + folder.getAbsolutePath());

            lastSelectedSong = Song.builder().name("<Select Song>").onlyText(true).build();
            songArrayList.add(lastSelectedSong);

			for(File file :  folder.listFiles()){
				if(file.isFile()){
					DebugLog.d(TAG, "Found: " + file.getName());

					Song song = Song.builder().name(file.getName())
							.location(folder.getAbsolutePath())
							.controllerSongParser(controllerByFileType(file.getAbsolutePath()))
							.build();
					songArrayList.add(song);
				}
			}
		}
		songArrayList.add(Song.builder().name("Download new song from URL...").onlyText(true).build());
	}

	private void initiateTracksList(){
		lastSelectedSongTracksList.add(SongTrack.builder().name("No song selected yet...").index(0).build());
	}

	private void updateTracks() {
		if (!lastSelectedSong.getOnlyText()) {
			ArrayAdapter<SongTrack> tracksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lastSelectedSong.getSongTrackList());
			guitarProTracksSpinner.setAdapter(tracksAdapter);
		}
	}


	private void enableLoopButton(Integer endHint) {
		if (lastSelectedSong.isGuitarProSong()) {
			currSongEndMeasure = endHint;
			loopButton.setEnabled(true);
			loopEndMeasureEditText.setHint(endHint.toString());
		}
	}

	private void resetLoopButton() {
		loopButton.setEnabled(false);
		loopStartMeasureEditText.setHint(R.string.default_measure_start);
		loopEndMeasureEditText.setHint(R.string.default_end_start);
	}


	private String prepareLoopInstruction(Integer startBar, Integer endBar) {
		return "~1/" + new StringBuilder(startBar.toString()).reverse().toString() + "/" + new StringBuilder(endBar.toString()).reverse().toString();
	}

	public void updateState(final String state){
		runOnUiThread(() -> {
			textViewState.setText(state);
			if (state.equals("Connection active, waiting for a song...")) {
				updateVariablesFromTextBoxes();
			}
		});
	}

	public void initiateVerifyButton() {
		verifyButton.setEnabled(true);
	}

}
