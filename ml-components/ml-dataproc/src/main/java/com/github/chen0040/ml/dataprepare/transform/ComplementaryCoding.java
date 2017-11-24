package com.github.chen0040.ml.dataprepare.transform;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.IntelliContext;

import java.util.List;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class ComplementaryCoding implements Cloneable {
    public double[] minValues;
    public double[] maxValues;

    public void copy(ComplementaryCoding rhs){
        minValues = rhs.minValues.clone();
        maxValues = rhs.maxValues.clone();
    }

    @Override
    public Object clone(){
        ComplementaryCoding clone = new ComplementaryCoding();
        clone.copy(this);

        return clone;
    }

    public ComplementaryCoding(){

    }

    public ComplementaryCoding(IntelliContext batch) {
        query(batch);
    }

    public ComplementaryCoding(List<double[]> batch){
        query(batch);
    }

    public double[] revert(double[] x){
        int m = x.length / 2;
        double[] y = new double[m];
        for(int i = 0; i < m ; ++i){
            y[i] = x[i] * (maxValues[i] - minValues[i]) + minValues[i];
        }
        return y;
    }

    public double[] normalize(double[] x){
        double[] y = new double[x.length * 2];
        for(int i = 0; i < x.length; ++i){
            y[i] = (x[i] - minValues[i]) / (maxValues[i] - minValues[i]);
        }
        for(int i=x.length; i < x.length * 2; ++i){
            y[i] = 1 - y[i-x.length];
        }
        return y;
    }

    protected void query(List<double[]> batch){
        int dimension = batch.get(0).length;
        int m = batch.size();

        //normalization
        minValues = new double[dimension];
        maxValues = new double[dimension];
        for (int i = 0; i < dimension; ++i) {
            minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }

        for (int i = 0; i < m; ++i) {
            double[] x = batch.get(i);
            for (int j = 0; j < dimension; ++j) {
                maxValues[j] = Math.max(x[j], maxValues[j]);
                minValues[j] = Math.min(x[j], minValues[j]);
            }
        }
    }

    protected void query(IntelliContext batch) {

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;
        int m = batch.tupleCount();

        //normalization
        minValues = new double[dimension];
        maxValues = new double[dimension];
        for (int i = 0; i < dimension; ++i) {
            minValues[i] = Double.MAX_VALUE;
            maxValues[i] = Double.MIN_VALUE;
        }

        for (int i = 0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            for (int j = 0; j < dimension; ++j) {
                maxValues[j] = Math.max(x[j], maxValues[j]);
                minValues[j] = Math.min(x[j], minValues[j]);
            }
        }
    }
}
