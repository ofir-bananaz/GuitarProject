package com.example.dannyboy.smartguitarapp;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
class SongTrack {
    private String name;
    private int index;


    public SongTrack(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Used to convert a SongTrack to String
     * @return
     */
    @Override
    public String toString(){
        return name;
    }

}
