package com.example.dannyboy.smartguitarapp;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public interface ControllerSongParser {

    int LAST_FRET = 8;//last_fret in their code
    int _VID = 87;
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

    Map<String, Integer> controllerUsedColors = ImmutableMap.of(
            "purple", _PRP,
            "red", _RDD,
            "green", _GRN,
            "blue", _BLU);

    ParsedSong getControllerStreamInner(Song song, int controllerTime, int trackIndex);

    default ParsedSong getControllerStream(Song song, int controllerTime, boolean isInteractiveMode, int trackIndex) {
        char interactiveChar = !isInteractiveMode ? (char) _NON_INTER : (char) _INTER;
        ParsedSong controllerStreamInner = getControllerStreamInner(song, controllerTime, trackIndex);
        String controllerStreamWithoutInteractive = interactiveChar + controllerStreamInner.getParsedString();
        return  ParsedSong.builder().parsedString(controllerStreamWithoutInteractive).measuresStartlist(controllerStreamInner.getMeasuresStartlist()).build();
    }

    List<SongTrack> getTrackNames(Song song);

}
