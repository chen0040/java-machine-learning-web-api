package com.github.chen0040.ml.commons.tune;

/**
 * Created by memeanalytics on 22/8/15.
 */
public class Tunable {

    private double minValue;
    private double maxValue;
    private double value;
    private String name;
    private String description;

    public Tunable(String name, double value, double minValue, double maxValue, String description){
        this.name = name;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.description = description;
    }




    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return getName();
    }

    public void setValue(double value) {
        this.value = value;
    }


    public double getValue(){
        return value;
    }


    public double getMinValue(){
        return minValue;
    }
    
    public double getMaxValue(){
        return maxValue;
    }

    public void setMinValue(double min){
        minValue = min;
    }

    public void setMaxValue(double max){
        maxValue = max;
    }

    public String getDescription(){
        return description;
    }


}
