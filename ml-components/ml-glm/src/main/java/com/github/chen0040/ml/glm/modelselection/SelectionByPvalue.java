package com.github.chen0040.ml.glm.modelselection;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class SelectionByPvalue {
    private double pValue;
    private int featureIndex;

    public double pValue() {
        return pValue;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    public SelectionByPvalue(int featureIndex, double pValue){
        this.featureIndex = featureIndex;
        this.pValue = pValue;
    }


}
