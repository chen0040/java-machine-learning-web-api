package com.github.chen0040.ml.linearalg;

import java.io.Serializable;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class IndexValue implements Serializable, Cloneable {
    private double value;
    private int index;

    public IndexValue(){
        index = -1;
        value = Double.NEGATIVE_INFINITY;
    }

    public IndexValue(int index, double value){
        this.index = index;
        this.value = value;
    }

    @Override
    public Object clone(){
        IndexValue clone = new IndexValue();
        clone.setValue(value);
        clone.setIndex(index);
        return clone;
    }

    @Override
    public boolean equals(Object rhs){
        if(rhs != null && rhs instanceof IndexValue){
            IndexValue rhs2 = (IndexValue)rhs;
            return index == rhs2.index && value == rhs2.value;
        }
        return false;
    }

    public boolean hasValue(){
        return index != -1;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public double getValue(){
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }
}
