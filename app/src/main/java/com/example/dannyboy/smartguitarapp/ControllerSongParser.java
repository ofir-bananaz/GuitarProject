package com.example.dannyboy.smartguitarapp;

public interface ControllerSongParser {

    int LAST_FRET = 8;//last_fret in their code
    int _PRP = 86;
    int _EOM = 85;
    int _HLD = 84;
    int _RDD = 83;
    int _GRN = 82;
    int _BLU = 81;
    int _OFF = 80;
    int _INTER = 97;
    int _NON_INTER = 100;
    int BACK_SLASH = 100;
    int TCP = 0;
    int UDP = 1;

    public String getControllerString(Song song, int controllerTime, boolean isInteractiveMode);

}
