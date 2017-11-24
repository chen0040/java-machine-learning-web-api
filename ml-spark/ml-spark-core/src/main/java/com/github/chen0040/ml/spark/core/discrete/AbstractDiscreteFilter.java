package com.github.chen0040.ml.spark.core.discrete;

import org.apache.spark.api.java.JavaRDD;

/**
 * Created by memeanalytics on 18/8/15.
 */
public abstract class AbstractDiscreteFilter implements DiscreteFilter {


    public AbstractDiscreteFilter(int index){
        this.index = index;
    }
    private int index;

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public abstract void build(JavaRDD<Double> rdd);

    public abstract int discretize(double value);
}
