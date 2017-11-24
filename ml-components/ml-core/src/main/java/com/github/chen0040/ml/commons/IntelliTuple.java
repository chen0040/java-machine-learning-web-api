package com.github.chen0040.ml.commons;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class IntelliTuple implements Cloneable {
    private String labelOutput;
    private String predictedLabelOutput;
    private Object tag;
    private Map<Integer, Double> model;
    private int length;
    private double numericOutput;
    private double predictedNumericOutput;
    private boolean hasNumericOutput = false;
    private int index = -1;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }



    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    IntelliTuple(int dimension){
        model = new HashMap<>();
        this.length = dimension;
    }

    IntelliTuple(double[] values, String classLabel){
        model = new HashMap<>();
        this.length = 0;

        setLabelOutput(classLabel);
        for(int i=0; i < values.length; ++i){
            set(i, values[i]);
        }
    }

    public Map<Integer, Double> getModel(){
        return model;
    }

    public void setModel(Map<Integer, Double> model){
        this.model = model;
    }

    public boolean hasNumericOutput(){
        return hasNumericOutput;
    }

    public boolean hasLabelOutput(){
        return getLabelOutput() != null;
    }

    public double getNumericOutput(){
        return numericOutput;
    }

    public void setNumericOutput(double output){
        hasNumericOutput = true;
        this.numericOutput = output;
    }



    public double getPredictedNumericOutput(){
        return predictedNumericOutput;
    }

    public void setPredictedNumericOutput(double value){
        predictedNumericOutput = value;
    }



    public void copy(IntelliTuple rhs){
        labelOutput = rhs.labelOutput;
        predictedLabelOutput = rhs.predictedLabelOutput;

        numericOutput = rhs.numericOutput;
        predictedNumericOutput = rhs.predictedNumericOutput;
        hasNumericOutput = rhs.hasNumericOutput;

        tag = rhs.tag;
        length = rhs.length;
        model = new HashMap<Integer, Double>(rhs.model);
        this.index = rhs.index;
    }

    @Override
    public Object clone(){
        IntelliTuple clone = new IntelliTuple(length);
        clone.copy(this);
        return clone;
    }

    public String getLabelOutput() {
        return labelOutput;
    }

    public void setLabelOutput(String label)
    {
        this.labelOutput = label;
    }

    public String getPredictedLabelOutput() {
        return predictedLabelOutput;
    }

    public void setPredictedLabelOutput(String predictedLabel) {
        this.predictedLabelOutput = predictedLabel;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public int tupleLength(){
        return length;
    }

    public Double get(int index, Double default_value){
        if(model.containsKey(index)){
            return model.get(index);
        }
        return default_value;
    }

    public void set(int index, Double value){
        model.put(index, value);
        if(length < index+1){
            length = index+1;
        }
    }













    public Object getAttributeValue(int index, Object default_value){
        if(model.containsKey(index)){
            return model.get(index);
        }
        return default_value;
    }

    public void resize(int tupleDimension) {
        length = tupleDimension;
    }
}
