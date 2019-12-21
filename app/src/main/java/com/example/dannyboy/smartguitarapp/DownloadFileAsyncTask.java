package com.example.dannyboy.smartguitarapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by SlimPC on 23-Dec-17.
 */

class DownloadFileAsyncTask extends AsyncTask<String, Void, Object[]>{
	private String TAG = "myFilter";
	private Context myContext;
	private OnDoneListener listener;

	/**
	 * Used when download song from URL. Must be used along a "download done" event listener that adds the new Song to the list of current selectable songs.
	 * @param context the caller context.
	 */
	public DownloadFileAsyncTask(Context context){
		myContext = context;
	}

	public Context getMyContext(){
		return myContext;
	}

	public void setOnDoneListener(OnDoneListener listener){
		this.listener = listener;
	}


	protected Object[] doInBackground(String... params){
		String fileName = params[0];
		String fileURL = params[1];
		String fileLocation="UNDEFINED";
		Object[] retObjects = new Object[2];
		Song newSong = new Song(fileName, fileLocation,fileURL);
		Integer retCode = -1;


		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		HttpsURLConnection connection = null;
		FileOutputStream outputStream1=null;
		try{
			URL url = new URL(fileURL);
			connection = (HttpsURLConnection) url.openConnection();
			connection.connect();


			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if(connection.getResponseCode() != HttpsURLConnection.HTTP_OK){
				Log.d(TAG, "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();

			// download the file
			inputStream = connection.getInputStream();


			outputStream = getMyContext().openFileOutput(fileName, Context.MODE_PRIVATE);//Context, verify this one is correct
			File file = new File(Environment.getExternalStorageDirectory(), "SmartGuitar/"+fileName);
			if(file.exists()){
				retCode=1;
			}
			else{
				outputStream1 = new FileOutputStream(file);
				byte data[] = new byte[4096];
				int count;
				while((count = inputStream.read(data)) != -1){
					outputStream.write(data, 0, count);
					outputStream1.write(data, 0, count);

				}
				newSong.setInDevice(true);
				Log.d(TAG, "File " + fileName + " download succeeded!");
				retCode = 0;
			}

		}catch(Exception e){

			Log.e(TAG, "Could not download file from URL: " + fileURL+"\n"+Log.getStackTraceString(e));


		}finally{
			try{
				if(outputStream1 != null)
					outputStream1.close();
				if(outputStream != null)
					outputStream.close();
				if(inputStream != null)
					inputStream.close();
			}catch(Exception e){
				Log.e(TAG, "Exception while closing input or output after downloading from URL");
			}

			if(connection != null)
				connection.disconnect();
		}
		retObjects[0] = retCode;
		retObjects[1] = newSong;
		return retObjects;
	}

	@Override
	protected void onPostExecute(Object[] result){
		listener.onDone(result);

	}

}