package com.example.dannyboy.smartguitarapp;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;


public class TextTabParser implements ControllerSongParser {


	private static int tempoDefault = 127;//last_fret in their code

	private static final int RADIX = 22 + 1;
	private static final Map<String, Integer> dictT = ImmutableMap.of("purple", _PRP,
			"red", _RDD,
			"green", _GRN,
			"blue", _BLU);


	private String bitString;
	public String[] tabs_six_lines;
	public List<Second> time_list = new ArrayList<Second>();
	//public Second[] time_list;
	private List<String> column_array = new ArrayList<String>();//FIXME is it ok putting it here
	// TODO Deal with argument as path, non relevent text in file, multiple lines

	public TextTabParser(Song song, int tempo) {//throws IOException{
		String TAG = "myFilter";
		List<String> lines;
		try{
			lines = changeFretNumRadix(song.getAbsolutePath());
		}catch (IOException e){
			DebugLog.e(TAG,"CRITICAL ERROR: Invalid song path");
			return;
		}

		if(tempo>0){
			tempoDefault =tempo;
		}

		List<String> list = new ArrayList<String>();
		for(String str : lines){
			Log.i(TAG, "Reading line:" + str);
			if(str.length() > 1){
				String lineStart = str.substring(0, 2);
				if(lineStart.equals("E|") || lineStart.equals("e|") || lineStart.equals("e-") || lineStart.equals("E-") || lineStart.equals("B|") || lineStart.equals("G|") || lineStart.equals("D|") || lineStart.equals("A|"))
					list.add(str);
			}
		}
		tabs_six_lines = list.toArray(new String[0]);

		for(int offset = 0; offset < Array.getLength(this.tabs_six_lines); offset += 6){
			this.parse_six_lines(offset);
		}

		DebugLog.d("myFilter", "Generating data...");
		this.add_strings();
		this.reverse_all_strings();
		this.shift_left_all();
		this.generateNonBinDataString();
	}

	private String[] joinInterleave(String[] s1, String[] s2){
		List<String> retList = new ArrayList<String>();

		Boolean prev = false;
		Boolean next = false;
		for(int i = 0; i < s2.length; i++){
			prev = false;
			next = false;
			String firstChar = s1[i].substring(0, 1);
			if(firstChar.equals((")"))){
				if(Integer.parseInt(s2[i - 1]) > 9){
					retList.add(firstChar + "-" + s1[i].substring(1));
				}else{
					retList.add(s1[i]);
				}
			}else{
				retList.add(s1[i]);
			}
			String prevChar = s1[i].substring(s1[i].length() - 1);
			String nextChar = s1[i + 1].substring(0, 1);
			prev = !prevChar.equals("-");
			next = !nextChar.equals("-");
			if(Integer.parseInt(s2[i]) > 9){
				String shrinked = Integer.toString(Integer.parseInt(s2[i]), RADIX).toUpperCase();
				if(prev == false && next == false){
					retList.add("-" + shrinked);
				}
				if(prev == false && next == true){
					retList.add("-" + shrinked);
				}
				if(prev == true && next == false){
					retList.add(shrinked + "-");
				}
				if(prev == true && next == true){
					retList.add(shrinked);
				}
			}else{
				retList.add(s2[i]);
			}


		}
		retList.add(s1[s1.length - 1]);
		return retList.toArray(new String[0]);
	}

	private String[] listCleaner(String[] inputList){
		List<String> retList = new ArrayList<String>();
		for(String str : inputList){
			if(str.equals(""))
				continue;
			else
				retList.add(str);
		}
		return retList.toArray(new String[0]);
	}

	private String getTabsFromLine(String line, String delimiter){
		String lineNoName = line;// line.substring(2, line.length()-1);
		String[] temp = listCleaner(line.replace(delimiter, "~" + delimiter + "~").split("~"));

		String[] allSigns = listCleaner(lineNoName.split("\\d+"));
		String[] allFrets = listCleaner(lineNoName.split("\\D+"));

		temp = joinInterleave(allSigns, allFrets);

		return TextUtils.join("", temp);
	}

	private List<String> changeFretNumRadix(String absolutePath) throws IOException{
		List<String> lines = new ArrayList<>();

		final FileInputStream fileInputStream = new FileInputStream(new File(absolutePath));
		BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream));
		String line;
		while((line = in.readLine()) != null){
			if(line.length() > 1){
				String fixedLine = getTabsFromLine(line, "-");
				lines.add(fixedLine);
			}else{
				lines.add(line);
			}
		}
		in.close();
		return lines;
	}


	public void parse_column_array(List<String> column_array){
		List<LookupArrayElement> LookupArray = new ArrayList<LookupArrayElement>();

		for(int i = 0; i < 6; i++){
			LookupArray.add(new LookupArrayElement());
		}

		for(int sec = 0; sec < column_array.size(); sec++){
			//List<Dot> dots = new ArrayList<Dot>();//dots=sec_list in their code
			Second newSec = new Second();
			//Parse second line after line
			for(int string = 0; string < 6; string++){
				String curChar = new String(column_array.get(sec).substring(string, string + 1));
				//This string is empty (should not be played, hence -)
				if(curChar.equals("-"))
					continue;

				//This string should is not empty, let's check what's in it:
				if(curChar.matches(".*\\d.*") || curChar.matches(".*[A-Z].*")){//Check if this string is a number
					int fret = Integer.parseInt(curChar, RADIX);
					//Hammer On
					if(LookupArray.get(string).getType().equals("ho")){
						//	handle_hammer_on(LookupArray, newSec);

						//Remove extra space
						for(Dot dot : LookupArray.get(string).getDotList()){
							time_list.get(time_list.size() - 2).removeDot(dot);
							time_list.get(time_list.size() - 1).addDot(dot);
						}
						//Add "prepare for hammer on" dot 1 sec before
						Dot prepareDot = new Dot(fret, string, "purple");
						time_list.get(time_list.size() - 1).addDot(prepareDot);

						//Add the last dot plus "play hammer on" dot to current second
						for(Dot dot : LookupArray.get(string).getDotList()){
							newSec.addDot(dot);
						}

						Dot playDot = new Dot(fret, string, "green");
						newSec.addDot(playDot);
						LookupArray.get(string).setType("none");
						continue;
					}
					//Pull Off
					if(LookupArray.get(string).getType().equals("po")){

						//Remove extra space
						for(Dot dot : LookupArray.get(string).getDotList())
							time_list.get(time_list.size() - 1).addDot(dot);
						//Add "prepare for hammer on" dot 1 sec before
						Dot newDot = new Dot(fret, string, "blue");
						time_list.get(time_list.size() - 1).addDot(newDot);

						//Add the last dot plus "play hammer on" dot to current second
						newSec.addDot(newDot);
						LookupArray.get(string).setType("none");
						continue;
					}
					//Slide Up
					if(LookupArray.get(string).getType().equals("su")){
						Dot newDot = new Dot(fret, string, "blue");
						int start_fret = LookupArray.get(string).getDotList().get(0).get_fret();
						Dot startDot = new Dot(start_fret, string, "blue");
						time_list.get(time_list.size() - 2).removeDot(newDot);
						time_list.get(time_list.size() - 1).addDot(newDot);
						light_from_to(start_fret + 1, fret, string, "purple", time_list.get(time_list.size() - 1));
						light_from_to(start_fret + 1, fret, string, "purple", newSec);
						newSec.addDot(newDot);
						LookupArray.get(string).setType("none");
						continue;
					}
					//Slide Down
					if(LookupArray.get(string).getType().equals("sd")){
						Dot newDot = new Dot(fret, string, "blue");
						int next_fret = fret;
						int start_fret = LookupArray.get(string).getDotList().get(0).get_fret();
						Dot startDot = new Dot(start_fret, string, "blue");
						time_list.get(time_list.size() - 2).removeDot(newDot);
						time_list.get(time_list.size() - 1).addDot(newDot);
						if(start_fret > 0)
							start_fret--;
						light_from_to(next_fret, start_fret, string, "purple", time_list.get(time_list.size() - 1));
						light_from_to(next_fret, start_fret, string, "purple", newSec);
						newSec.addDot(newDot);
						LookupArray.get(string).setType("none");
						continue;
					}
					//Bend
					if(LookupArray.get(string).getType().equals("b")){
						for(Dot dot : LookupArray.get(string).getDotList()){
							time_list.get(time_list.size() - 2).removeDot(dot);
						}
						int upper_fret = LookupArray.get(string).getDotList().get(0).get_fret();
						int upper_string = string;
						if(upper_string == 0)
							upper_string = 1;
						else
							upper_string--;
						Dot upper_dot = new Dot(upper_fret, upper_string, "purple");
						newSec.addDot(upper_dot);
						for(Dot dot : LookupArray.get(string).getDotList())
							newSec.addDot(dot);
						LookupArray.get(string).setType("none");
						continue;
					}
					//Release
					if(LookupArray.get(string).getType().equals("r")){
						int upper_string = string;
						if(upper_string == 0)
							upper_string = 1;
						else
							upper_string--;
						Dot upper_dot = new Dot(fret, upper_string, "purple");
						Dot dot = new Dot(fret, string, "blue");
						Dot prev_dot = new Dot(fret, string, "purple");
						time_list.get(time_list.size() - 1).addDot(prev_dot);
						time_list.get(time_list.size() - 1).addDot(upper_dot);
						newSec.addDot(dot);
						continue;
					}
					//Vibrato
					if(LookupArray.get(string).getType().equals("v")){
						for(Dot dot : LookupArray.get(string).getDotList()){
							time_list.get(time_list.size() - 2).removeDot(dot);
						}
						int newFret = LookupArray.get(string).getDotList().get(0).get_fret();

						Dot dot = new Dot(newFret, string, "green");
						newSec.addDot(dot);
						continue;
					}
					//Ghost note
					if(LookupArray.get(string).getType().equals("gn")){
						Dot dot = new Dot(fret, string, "red");
						newSec.addDot(dot);
						LookupArray.get(string).setType("none");
						continue;
					}

					if(LookupArray.get(string).getType().equals("none")){
						Dot dot = new Dot(fret, string, "blue");
						//dots.add(dot);
						newSec.myDots.add(dot);
						continue;
					}
				}

				//Slide up
				if(curChar.equals("/")){
					LookupArray.get(string).setType("su");
					int start_fret = 1;//TODO change to 1 according to the actual fret array
					String chr = column_array.get(sec - 1).substring(string, string + 1);
					if(chr.matches(".*\\d+.*") && column_array.size() > 2)//TODO: figure out why greater than 2
						start_fret = Integer.parseInt(chr, RADIX);
					LookupArray.get(string).insertDot(new Dot(start_fret, string, "purple"));
					continue;
				} //Slide down
				if(curChar.equals("\\")){
					LookupArray.get(string).setType("sd");
					int start_fret = 0;//TODO change to 1 according to the actual fret array
					String chr = column_array.get(sec - 1).substring(string, string + 1);
					if(chr.matches(".*\\d+.*") && column_array.size() > 2)//TODO: figure out why greater than 2
						start_fret = Integer.parseInt(chr, RADIX);
					LookupArray.get(string).insertDot(new Dot(start_fret, string, "purple"));
					continue;
				}
				//Hammer on
				if(curChar.equals("h")){
					LookupArray.get(string).setType("ho");
					int prev_fret = getFret(sec - 1, string);
					LookupArray.get(string).insertDot(new Dot(prev_fret, string, "blue"));
					continue;
				}
				//Pull off
				if(curChar.equals("p")){
					LookupArray.get(string).setType("po");
					int prev_fret = Integer.parseInt(column_array.get(sec - 1).substring(string, string + 1), RADIX);
					LookupArray.get(string).insertDot(new Dot(prev_fret, string, "purple"));
					continue;
				}
				//Ghost note
				if(curChar.equals("(")){
					LookupArray.get(string).setType("gn");
					continue;
				} //Band
				if(curChar.equals("b") || curChar.equals("^")){
					LookupArray.get(string).setType("b");
					int prev_fret = Integer.parseInt(column_array.get(sec - 1).substring(string, string + 1), RADIX);
					LookupArray.get(string).insertDot(new Dot(prev_fret, string, "blue"));
					continue;
				} //Release
				if(curChar.equals("r")){
					LookupArray.get(string).setType("r");
					int prev_fret = Integer.parseInt(column_array.get(sec - 1).substring(string, string + 1), RADIX);
					LookupArray.get(string).insertDot(new Dot(prev_fret, string, "purple"));
					continue;
				}
				//Vibrato
				if(curChar.equals("v") || curChar.equals("~")){
					LookupArray.get(string).setType("b");
					//int prev_fret=Integer.parseInt( column_array.get(sec-1).substring(string, string+1),RADIX);
					int prev_fret = getFret(sec - 1, string);
					Dot newDot = new Dot(prev_fret, string, "green");
					LookupArray.get(string).insertDot(new Dot(prev_fret, string, "blue"));
					//Remove this dot from time list, we're gonna insert a fixed version soon?
					continue;
				}

			}

			time_list.add(newSec);
		}

	}

	int getFret(int time, int stringNumber){
		return Integer.parseInt(column_array.get(time).substring(stringNumber, stringNumber + 1));
	}

	public void parse_six_lines(int offset){

		int line_len = tabs_six_lines[offset].length();// consider null char as added to length

		for(int col = 1; col < line_len; col++){
			StringBuilder column = new StringBuilder();
			for(int line = offset; line < 6 + offset; line++){
				column.append(tabs_six_lines[line].charAt(col));
			}
			if(column.toString().equals("||||||"))
				continue;
			if(column.toString().equals("------")){// Detect new macro
				if(column_array.isEmpty()){

					Second emptySecond = new Second();
					time_list.add(emptySecond);
					// column_array.clear(); TODO: check if needed.
					continue;
				}else{
					parse_column_array(column_array); // Add all of the macros seconds
					column_array.clear();
					Second eomSec = new Second();
					eomSec.setEom(true);
					time_list.add(eomSec);
					continue;

				}

			}else
				column_array.add(column.toString());

		}

	}

	public void add_strings(){
		for(int i = 0; i < time_list.size(); i++){
			Second curSecond = time_list.get(i);
			if(curSecond.isEmpty() || curSecond.isEOM())
				continue;
			else{
				for(int j = 0; j < curSecond.myDots.size(); j++){
					Dot curDot = curSecond.myDots.get(j);
					if(curDot.get_color().equals("blue") || curDot.get_color().equals("green") || curDot.get_color().equals("red")){
						Dot ctrlDot = new Dot(0, curDot.get_string(), "purple");//ctrlDot=string_dot
						if(!curSecond.isDotInMyDots(ctrlDot))
							curSecond.myDots.add(ctrlDot);
					}

				}
			}
		}
	}

	public void reverse_all_strings(){
		for(Second sec : time_list){
			if(sec.isEmpty() || sec.isEOM())
				continue;
			for(Dot dot : sec.myDots)
				if(!dot.isReversed())
					dot.reverse_string().setReversed(true);
		}
	}

	public void shift_left_all(){
		for(Second sec : time_list){
			if(sec.isEmpty() || sec.isEOM())
				continue;
			for(Dot dot : sec.myDots)
				if(!dot.isShifted()){
					if(dot.get_fret() == 0)
						dot.set_fret(LAST_FRET - 1);
					else
						dot.set_fret(dot.get_fret() - 1);
					dot.setShifted(true);
				}
		}
	}

	public void generateNonBinDataString(){
		StringBuilder sent_stringBuilder = new StringBuilder();

		for(int i = 0; i < time_list.size(); i++){
			Second curSecond = time_list.get(i);
			if(curSecond.isEmpty()){ // <--- This line creates noise!
				sent_stringBuilder.append((char) _HLD);
				sent_stringBuilder.append((char) tempoDefault);

			} else if(curSecond.isEOM()){
				sent_stringBuilder.append((char) _HLD);
				sent_stringBuilder.append((char) tempoDefault);
				sent_stringBuilder.append((char) _EOM);

			} else {
				for(int j = 0; j < curSecond.myDots.size(); j++) {
					Dot curDot = curSecond.myDots.get(j);
					if (dictT.containsKey(curDot.get_color())) {
						sent_stringBuilder.append((char) ((int) dictT.get(curDot.get_color())));
					} else {
						throw new RuntimeException("Color does not exist");
					}
					sent_stringBuilder.append((char) curDot.get_fret());
					sent_stringBuilder.append((char) curDot.get_string());
				}
				sent_stringBuilder.append((char) _HLD);
				sent_stringBuilder.append((char) tempoDefault);
			}
		}
		this.bitString = sent_stringBuilder.toString();
	}

	private void light_from_to(int start_fret, int end_fret, int string, String color, Second sec){
		if(start_fret > end_fret){
			int temp = start_fret;
			start_fret = end_fret;
			end_fret = temp;
		}
		for(int fret = 0; fret < end_fret + 1; fret++){
			Dot new_dot = new Dot(fret, string, color);
			sec.addDot(new_dot);
		}

	}

	public String sendToGuitar(Activity mainActiviry, String IP, String port,Boolean interactive_mode){
		DebugLog.d("myFilter", "Sending data to controller...");

		String serverAnswer = "null";
		try{
			return (new SendDataToControllerAsyncTask(mainActiviry)).execute("UDP", IP, port, bitString).get();
		}catch(Exception e){
			Log.e("myFilter", "Exception in sendDataToControllerAsyncTask");
		}
		return serverAnswer;
	}

	public String getControllerString(boolean isInteractiveMode) {
		if(!isInteractiveMode){
			return ((char) _NON_INTER) + bitString;
		}else{
			return ((char) _INTER) + bitString;
		}
	}

}
