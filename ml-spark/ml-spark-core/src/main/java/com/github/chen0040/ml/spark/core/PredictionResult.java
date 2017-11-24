package com.github.chen0040.ml.spark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 11/5/15.
 */
public class PredictionResult implements Serializable, Cloneable {
    private double value;
    private String label;
    private List<Double> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Map<String, String> features = new HashMap<>();

    @Override
    public Object clone(){
        PredictionResult clone = new PredictionResult();
        clone.copy(this);
        return clone;
    }

    public void copy(PredictionResult rhs){
        value = rhs.value;
        label = rhs.label;
        values.clear();
        for(int i=0; i < rhs.values.size(); ++i){
            values.add(rhs.values.get(i));
        }
        labels.clear();
        labels.addAll(rhs.labels);

        this.features.clear();
        for(Map.Entry<String, String> entry : rhs.features.entrySet()){
            features.put(entry.getKey(), entry.getValue());
        }

    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void addFeature(String featureName, String featureValue){
        features.put(featureName, featureValue);
    }

}
