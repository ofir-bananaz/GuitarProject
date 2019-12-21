package com.example.dannyboy.smartguitarapp;

/**
 * Created by SlimPC on 21-Dec-17.
 * Used as for a LED/tab fret/string coordinate
 */

public class Dot {

    private int _fret;
    private int _string;
    private String _color;
    private boolean _isReversed;
    private boolean _isShifted;

    public Dot(Integer fret, Integer string, String color) {
        _fret = fret;
        _string = string;
        _color = color;
        _isReversed=false;
        _isShifted=false;
    }

    public Dot() {
        _fret = -1;
        _string = -1;
        _color = "undefined";

    }

    public Dot(Boolean EOM) {
        _fret = -1;
        _string = -1;
        _color = "undefined";

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dot other = (Dot) obj;
        if (_color == null) {
            if (other._color != null)
                return false;
        } else if (!_color.equals(other._color))
            return false;
        if (_fret != other._fret)
            return false;
		return _string == other._string;
	}
    @Override
    public int hashCode() {
        final int prime = 33;
        int result = 1;
        result = prime * result + ((_color == null) ? 0 : _color.hashCode());
        result = prime * result + _fret;
        result = prime * result + _string;
        return result;
    }

    public int get_fret() {
        return _fret;
    }

    public void set_fret(Integer _fret) {
        this._fret = _fret;
    }

    public int get_string() {
        return _string;
    }

    public void set_string(Integer _string) {
        this._string = _string;
    }

    public String get_color() {
        return _color;
    }

    public void set_color(String _color) {
        this._color = _color;
    }

    public Dot reverse_string() {
        _string = 5 - _string;
        return this;
    }
    @Override
    public String toString(){
        return "("+((Integer) _fret).toString()+" ,"+((Integer) _string).toString()+" ,"+_color+")";
    }
    public boolean isReversed(){
        return _isReversed;
    }
    public void setReversed(boolean state){
        _isReversed=state;
    }
    public boolean isShifted(){
        return _isShifted;
    }
    public void setShifted(boolean state){//d
        _isShifted=state;

    }
}