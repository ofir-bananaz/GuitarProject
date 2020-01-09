package com.example.dannyboy.smartguitarapp;

/**
 * Created by SlimPC on 22-Dec-17.
 */

import lombok.Builder;

/**
 * Song is a basic object used to keep a song meta-data.
 */
@Builder
public class Song {
    private String name;
    private String location;
    private Boolean inGuitar;
    private ControllerSongParser controllerSongParser;

    /**
     * Checks if this song has already been sent to the guitar controller
     * @return true - the song has been sent <p> false - otherwise</p>
	 *
     */
    public Boolean isInGuitar(){
        return inGuitar;
    }

	/**
	 * Mark the song as sent, used after sending a song to the guitar controller, or after the controller has done playing it (stopped or finished).
	 * @param _inGuitar - Use true to mark it as sent false otherwise
	 */
    public void setInGuitar(Boolean _inGuitar){
        this.inGuitar = _inGuitar;
    }


	/**
	 * @param name Song file name
	 * @param location Location on the device
	 */
    Song(String name, String location, Boolean inGuitar, ControllerSongParser controllerSongParser){
        this.name = name;
        this.location = location;
        this.inGuitar = false;
        this.controllerSongParser = controllerSongParser;
    }

	/**
	 * Used to get the complete path of a Song
	 * @return path as a String.
	 */
	public String getAbsolutePath(){
	    return location + "/" + name;
	}


	/**
	 * Used to get a song filename
	 * @return
	 */
    public String getName() {
        return name;
    }

	/**
	 * Used to convert a Song to String
	 * @return
	 */
	public String toString(){
        return name;
    }


	/**
	 * An comperason method between two Song objects
	 * @param o Song to compare to
	 * @return True when equal, false otherwise
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song Song = (Song) o;
        if(name.equals("<Select Song>"))
            return true;
        if(Song.name.equals("<Select Song>"))
            return false;
        return name.equals(Song.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String prepareSongForController(boolean isInteractive, int controllerTime) {
        return controllerSongParser.getControllerString(this, controllerTime, isInteractive);
    }
}
