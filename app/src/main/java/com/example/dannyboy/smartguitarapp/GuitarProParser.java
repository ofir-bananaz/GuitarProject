package com.example.dannyboy.smartguitarapp;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuitarProParser implements ControllerSongParser{
    private static final GuitarProParser ourInstance = new GuitarProParser();

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

    public void loadingProgression(double percent) {
        long iPart = (long) percent;
        String msg = "Loading song ... " + iPart + "%";
        DebugLog.d("myFilter", msg);
    }


    public String generateControllerSongStream(List<PyObject> events, int controllerTime){
        StringBuilder sent_stringBuilder = new StringBuilder();

        int eventsProgressionCounter = events.size() /8;
        int i = 0;
        for(PyObject event : events){
            i++;
            if ( (i % eventsProgressionCounter) == 0) {
                loadingProgression((i*100) / events.size());
            }

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
                    time = Objects.requireNonNull(eventMap.get("time")).toInt(); // TODO - change so that real time is used
                    sent_stringBuilder.append((char) controllerTime);
                    break;
                case "dot":
                    int fret = Objects.requireNonNull(eventMap.get("fret")).toInt();
                    int guitarString = Objects.requireNonNull(eventMap.get("guitar_string")).toInt();
                    String color = Objects.requireNonNull(eventMap.get("color")).toJava(String.class);
                    sent_stringBuilder.append((char) ((int) controllerUsedColors.get(color)));
                    sent_stringBuilder.append((char) fret);
                    sent_stringBuilder.append((char) guitarString);
                    break;
            }
        }
        return sent_stringBuilder.toString();
    }

    @Override
    public String getControllerStreamInner(Song song, int controllerTime, int trackIndex) {
        PyObject parser = getSongParserObject(song);
        int numberOfEvents = parser.callAttr("parse_to_seconds", trackIndex).toInt();
        List<PyObject> events = Objects.requireNonNull(parser.get("events")).asList();
        return generateControllerSongStream(events, controllerTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public List<String> getTrackNames(Song song) {
        PyObject parser = getSongParserObject(song);
        List<PyObject> fetch_tracks_names = parser.callAttr("fetch_tracks_names").asList();
        return fetch_tracks_names.stream().map(x -> x.toJava(String.class)).collect(Collectors.toList());
    }
}
