package com.example.dannyboy.smartguitarapp;

import com.chaquo.python.Python;

class GuitarProParser implements ControllerSongParser{
    private static final GuitarProParser ourInstance = new GuitarProParser();

    static GuitarProParser getInstance() {
        return ourInstance;
    }

    private GuitarProParser() {
    }


    @Override
    public String getControllerString(Song song, int tempo, boolean isInteractiveMode) {
        Python py = Python.getInstance();
        String parsedSong = py.getModule("myParser").callAttr("parse", "m a d e -  w i t h - p y t h o n").toJava(String.class);
        DebugLog.d("", "Not implemented yet - but the following string is " + parsedSong);
        return null;
    }
}
