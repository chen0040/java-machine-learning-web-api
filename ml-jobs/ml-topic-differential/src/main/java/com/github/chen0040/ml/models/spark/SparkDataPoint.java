package com.github.chen0040.ml.models.spark;

/**
 * Created by root on 11/9/15.
 */
public class SparkDataPoint {
    public int index;
    public double value;
    public String label;
    
    public SparkDataPoint(){
    	
    }

    public SparkDataPoint(int index, String label, double value){
        this.index = index;
        this.value = value;
        this.label = label;
    }
}
