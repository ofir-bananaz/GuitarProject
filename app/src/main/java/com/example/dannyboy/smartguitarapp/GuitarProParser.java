package com.example.dannyboy.smartguitarapp;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuitarProParser implements ControllerSongParser{
    private static final GuitarProParser ourInstance = new GuitarProParser();
    private static final int NUMBER_OF_GUITAR_STRINGS = 6;

    static GuitarProParser getInstance() {
        return ourInstance;
    }

    private Python py = Python.getInstance();

    private PyObject getSongParserObject(Song song) {
        PyObject guitarProControllerParser = py.getModule("GuitarProControllerParser");
        return guitarProControllerParser.callAttr("GuitarProControllerParser", song.getAbsolutePath());
    }


    /**
     *
     * @param eventMap
     * @param controllerTime
     * @return
     */
    private int calculateTime(Map<PyObject, PyObject> eventMap, int controllerTime) {
        int duration = Objects.requireNonNull(eventMap.get("time")).toInt();
        return ((60/ controllerTime) / 256) * (1/duration);
    }


    public String generateControllerSongStream(List<PyObject> events, int controllerTime){
        StringBuilder sent_stringBuilder = new StringBuilder();

        for(PyObject event : events){

            Map<PyObject, PyObject> eventMap = event.asMap();
            String eventType = Objects.requireNonNull(eventMap.get("event_type")).toString();
            int time;
            switch (eventType) {
                case "eom":
                    sent_stringBuilder.append((char) _HLD);
                    sent_stringBuilder.append((char) controllerTime); // TODO - change to time that is set in the
                    sent_stringBuilder.append((char) _EOM);
                    break;
                case "hold":
                    sent_stringBuilder.append((char) _HLD);
                    time = Objects.requireNonNull(eventMap.get("time")).toInt(); // TODO - change so that real time is used
                    sent_stringBuilder.append((char) controllerTime);
                    break;
                case "vid":
                    sent_stringBuilder.append((char) _VID);
                    time = Objects.requireNonNull(eventMap.get("time")).toInt();
                    sent_stringBuilder.append((char) time);
                    break;
                case "dot":
                    int fret = Objects.requireNonNull(eventMap.get("fret")).toInt();
                    int guitarStringReversed = Objects.requireNonNull(eventMap.get("guitar_string")).toInt();
                    int guitarStringControllerIdx = NUMBER_OF_GUITAR_STRINGS - guitarStringReversed; // reverse all strings as controller was receives everything upside down
                    String color = Objects.requireNonNull(eventMap.get("color")).toJava(String.class);
                    sent_stringBuilder.append((char) ((int) controllerUsedColors.get(color)));
                    sent_stringBuilder.append((char) fret);
                    sent_stringBuilder.append((char) guitarStringControllerIdx);
                    break;
            }
        }
        return sent_stringBuilder.toString();
    }

    @Override
    public ParsedSong getControllerStreamInner(Song song, int controllerTime, int trackIndex) {
        PyObject parser = getSongParserObject(song);
        List<PyObject> measuresStartPy = parser.callAttr("parse_to_seconds", trackIndex).asList();
        List<PyObject> events = Objects.requireNonNull(parser.get("events")).asList();
        return ParsedSong.builder().parsedString(generateControllerSongStream(events, controllerTime)).measuresStartlist(toIntegerList(measuresStartPy)).build();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public List<SongTrack> getTrackNames(Song song) {
        PyObject parser = getSongParserObject(song);

        List<PyObject> fetch_tracks_names = parser.callAttr("fetch_tracks_names").asList();
        int index = 0;
        List<SongTrack> tracks = new ArrayList<>();
        for (PyObject track : fetch_tracks_names) {
            if (!track.toJava(String.class).equals("invalid track")) {
                tracks.add(SongTrack.builder()
                        .index(index)
                        .name(track.toJava(String.class)).build());
            }
            index++;
        }
        return tracks;
    }


    public List<Integer> toIntegerList(List<PyObject> measuresStartPy) {
        List<Integer> result = new ArrayList<>();
        for (PyObject measure : measuresStartPy) {
            result.add(measure.toInt());
        }
        return result;
    }
}
