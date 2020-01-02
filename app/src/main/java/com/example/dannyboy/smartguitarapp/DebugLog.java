package com.example.dannyboy.smartguitarapp;

import android.util.Log;
import android.widget.TextView;

/**
 * Used as Logger. Stores all messages passed to it, and outputs them in a sort, formatted manner (as a list).
 */
public class DebugLog{

	private static boolean DEBUG = true;
	private static TextView debugView = null;
	private static String debugLogHistory = null;
	private static Integer lines=0;

	/**
	 * Used as Logger. Stores all messages passed to it, and outputs them in a sort, formatted manner (as a list).
	 * <p>Currently, only 2 severity levels are supported: d - debug, e - exception</p>
	 * @param debug debug flag.
	 * @param debugViewIn TextView used to output the log to.
	 */
	public DebugLog(boolean debug, TextView debugViewIn){
		DEBUG = debug;
		debugView = debugViewIn;
		debugLogHistory = "";
	}

	/**
	 * Used when the message passed to the logger is of debug severity level
	 * @param TAG message Tag
	 * @param message message content
	 */
	public static void d(String TAG, String message){
		if(DEBUG){
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.d(TAG, className + "." + methodName + "(), line " + lineNumber + ": " + message);
			if(debugView != null){
				updateDebugLog(TAG,message);
			}
		}
	}


	/**
	 * Used when the message passed to the logger is of exception severity level
	 * @param TAG message Tag
	 * @param message message content
	 */
	public static void e(String TAG, String message){
		if(DEBUG){
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();

			Log.e(TAG, className + "." + methodName + "(), line " + lineNumber + ": " + message);
			if(debugView != null){
				updateDebugLog(TAG,message);
			}
		}
	}
	/**
	 * Used when the message passed to the logger is of exception severity level including the exception log
	 * @param TAG message Tag
	 * @param message message content
	 */
	public static void e(String TAG, String message, Exception e){
		if(DEBUG){
			String fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
			String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
			String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
			int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
			Log.e(TAG, className + "." + methodName + "(), line " + lineNumber + ": " + message + "  Exception reason: " + Log.getStackTraceString(e).substring(0, Log.getStackTraceString(e).indexOf("\n")));
			if(debugView != null){
				updateDebugLog(TAG,message);
			}
		}
	}

	/**
	 * Used internaly to update the debugLog logger
	 * @param TAG message Tag
	 * @param message the actual message
	 */
	//This method MUST be public.
	public static void updateDebugLog(String TAG, String message){
		lines++;
		StringBuilder updatedLog = new StringBuilder();
		try{
			updatedLog.append(DebugLog.formatMessage(lines, message));
		}catch(Exception e){
			Log.e(TAG, "Exception in updateDebugLog!",e);
		}
		updatedLog.append(debugLogHistory);
		debugLogHistory=updatedLog.toString();
		debugView.setText(debugLogHistory);

	}

	/**
	 * Used internaly to format all the messages as a numbered list where each message is numbered according to order of creation.
	 * @param id message id (i.e. serial number)
	 * @param message actual message
	 */
	private static String formatMessage(Integer id,String message){
		boolean firstLine=true;
		int lineLength=40;
		int totalLines=(int)Math.ceil(message.length()/((float)lineLength));

		StringBuilder builder = new StringBuilder();
		for(int line=0; line<totalLines; line++){
			String currentLine="";
			int posStart=line*lineLength;
			int posEnd=(line+1)*lineLength;
			if (posEnd>message.length()){
				posEnd=message.length();
			}
			if (firstLine) {
				currentLine=String.format("%3d %s\n",id,message.substring(posStart,posEnd));
				firstLine=false;
			}
			else {
				currentLine=String.format("    %s\n",message.substring(posStart,posEnd));
			}
			builder.append(currentLine);
		}
		return builder.toString();
	}

}