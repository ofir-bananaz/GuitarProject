package com.example.dannyboy.smartguitarapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;

import static java.lang.Thread.sleep;

/**
 * Created by SlimPC on 23-Dec-17.
 * Import Song dialog
 */

public class PromptDialog{
	private String _songName;
	private String _songURL;
	private Context _myContext;
	private Integer _res;
	public AlertDialog alertDialog;

	/**
	 * Import song dialog
	 * @param context - caller context
	 */
	public PromptDialog(Context context){
		_songName = "undefined";
		_songURL = "undefined";
		_myContext = context;
		_res = 0;
		alertDialog =null;
	}

//	public String getSongName(){
//		return _songName;
//	}
//
//	public String getSongURL(){
//		return _songURL;
//	}
//
//	public Integer getRes(){
//		return _res;
//	}

	void promptUser(final DownloadFileAsyncTask downloadFileAsyncTask){


		final EditText txtUrl = new EditText(_myContext);
		final EditText txtName = new EditText(_myContext);

		LinearLayout layout = new LinearLayout(_myContext);
		layout.setOrientation(LinearLayout.VERTICAL);


		//txtName.setHint("Song name");
		txtName.setHint("simple_ours_online.txt");
		layout.addView(txtName);


		txtUrl.setHint("https://pastebin.com/raw/Pb9VejjQ");
		layout.addView(txtUrl);


		alertDialog = new AlertDialog.Builder(_myContext).setTitle("Download Song").setMessage("Paste in the song URL").setView(layout).setPositiveButton("Download", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				if((_songURL = txtUrl.getText().toString()).isEmpty())
					_songURL = txtUrl.getHint().toString();
				if((_songName = txtName.getText().toString()).isEmpty())
					_songName = txtName.getHint().toString();
				try{

					downloadFileAsyncTask.execute(_songName, _songURL);
				}catch(Exception e){
					DebugLog.e("myFilter", "Couldn't download  song from url: " + _songURL);
				}
				_res = 1;//User tapped DOWNLOAD. Now we're TRYING to download in background
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				_res = -1;//User tapped CANCEL. Now back to mainmenu
			}
		}).show();



		return ;

	}

}
