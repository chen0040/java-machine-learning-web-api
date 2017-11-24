package com.github.chen0040.ml.dataprepare.discretize.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 18/8/15.
 */
public abstract class AbstractDiscreteFilter implements DiscreteFilter {
    protected List<Double> values;

    public AbstractDiscreteFilter(int index){
        this.index = index;
        values = new ArrayList<Double>();
    }

    public void addValue(double value){
        values.add(value);
    }

    private int index;

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public abstract void build();

    public abstract int discretize(double value);
}
