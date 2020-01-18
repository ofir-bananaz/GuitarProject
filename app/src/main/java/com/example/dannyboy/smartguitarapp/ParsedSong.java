package com.example.dannyboy.smartguitarapp;


import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
class ParsedSong {

    private final String parsedString;
    private final List<Integer> measuresStartlist;

    ParsedSong(String parsedString, List<Integer> measuresStartlist){
        this.parsedString = parsedString;
        this.measuresStartlist = measuresStartlist;
    }
}
