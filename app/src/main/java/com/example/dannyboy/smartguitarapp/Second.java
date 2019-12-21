package com.example.dannyboy.smartguitarapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SlimPC on 21-Dec-17.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Second {
    private Boolean _isEOM;
    public List<Dot> myDots;

    public Second() {
        _isEOM = false;
        myDots = new ArrayList<Dot>();//change to private as it should be!

    }

    public Boolean isEmpty() {
		return !_isEOM && myDots.isEmpty();
    }

    public Boolean isEOM() {
        return _isEOM;
    }

    public void setEom(Boolean EOM) {
        _isEOM = EOM;
    }

    public boolean isDotInMyDots(Dot dot) {

        return myDots.contains(dot);

    }

    public Second removeDot(Dot dot) {

        myDots.remove(dot);
        return this;

    }

    public Second addDot(Dot dot) {
        myDots.add(dot);
        return this;

    }

    @Override
    public String toString() {
        String ret = "[";
        if (!_isEOM) {
            Iterator<Dot> it = myDots.iterator();
            if (it.hasNext()) {
                ret += it.next().toString();
            }
            while (it.hasNext()) {
                ret += (", " + it.next());
            }
            ret += "]";
            return ret;
        }
        return "'EOM'";
    }
}