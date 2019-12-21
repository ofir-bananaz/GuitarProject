package com.example.dannyboy.smartguitarapp;

/**
 * Created by dannyboy on 06/01/2018.
 */

import java.util.ArrayList;
import java.util.List;

public class LookupArrayElement {
    private String _type;
    private List<Dot> dot_list = new ArrayList<Dot>();

    public LookupArrayElement(){
        _type="none";
    }

    public LookupArrayElement(String type){
        _type=type;
    }
    public String getType(){
        return _type;
    }
    public void setType(String type){
        _type=type;
        if(type.equals("none")){
            dot_list.clear();
        }
    }
    public List<Dot> getDotList(){
        return dot_list;
    }
    public void insertDot(Dot dot){
        dot_list.add(dot);

    }

}