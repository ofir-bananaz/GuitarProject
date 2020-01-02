package com.example.dannyboy.smartguitarapp;

/**
 * Created by SlimPC on 22-Dec-17.
 */

/**
 * Song is a basic object used to keep a song meta-data.
 */
public class Song {
    private String _name;
    private String _url;
    private String _location;
    private Boolean _isInDevice;
    private Boolean _inGuitar;

    /**
     * Checks if this song has already been sent to the guitar controller
     * @return true - the song has been sent <p> false - otherwise</p>
	 *
     */
    public Boolean isInGuitar(){
        return _inGuitar;
    }

	/**
	 * Mark the song as sent, used after sending a song to the guitar controller, or after the controller has done playing it (stopped or finished).
	 * @param _inGuitar - Use true to mark it as sent false otherwise
	 */
    public void setInGuitar(Boolean _inGuitar){
        this._inGuitar = _inGuitar;
    }


	/**
	 * Mark the song as residing in device storage - used to ensure we don't use a song before its download has been complete (e.g. when importing from URL)
	 * @param _isInDevice - Use true to mark it as resides in the device storage
	 */
    public void setInDevice(Boolean _isInDevice) {
        this._isInDevice = _isInDevice;
    }

	/**
	 * @param name Song file name
	 * @param location Location on the device
	 * @param URL	Song URL
	 */
    Song(String name, String location, String URL){
        _name=name;
        _location=location;
        _isInDevice=false;
        _url=URL;
        _inGuitar =false;
    }
    Song(String name, String location){
        _name=name;
        _location=location;
        _isInDevice=true;
        _url="LOCAL_FILE";
        _inGuitar =false;
    }
	Song(String name){
		_name=name;
		_url="UNDEFINED_URL";
		_location="UNDEFINED_LOCATION";
		_isInDevice=false;
        _inGuitar =false;

	}
    Song(){
        _name="UNDEFINED_NAME";
        _url="UNDEFINED_URL";
        _location="UNDEFINED_LOCATION";
        _isInDevice=false;
        _inGuitar =false;

    }

	/**
	 * Used to get the complete path of a Song
	 * @return path as a String.
	 */
//    public Boolean isInDevice(){
//        return _isInDevice;
//    }
//    public String getURL() {
//        return _url;
//    }
//	public String getLocation(){return _location;}
	public String getAbsolutePath(){return _location+"/"+_name;}


	/**
	 * Used to get a song filename
	 * @return
	 */
    public String getName() {
        return _name;
    }

	/**
	 * Used to convert a Song to String
	 * @return
	 */
	public String toString(){
        return _name;
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
        if(_name.equals("<Select Song>"))
            return true;
        if(Song._name.equals("<Select Song>"))
            return false;
        return _name.equals(Song._name);
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}
