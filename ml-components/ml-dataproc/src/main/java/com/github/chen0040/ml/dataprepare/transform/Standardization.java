package com.github.chen0040.ml.dataprepare.transform;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;

import java.util.List;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class Standardization implements Cloneable {
    public double[] mu;
    public double[] std;

    public void copy(Standardization rhs){
        mu = rhs.mu.clone();
        std = rhs.std.clone();
    }

    @Override
    public Object clone(){
        Standardization clone = new Standardization();
        clone.copy(this);

        return clone;
    }

    public Standardization(){

    }

    public Standardization(IntelliContext batch) {
        query(batch);
    }

    public Standardization(List<double[]> batch){
        query(batch);
    }

    public double[] revert(double[] x){
        double[] y = new double[x.length];
        for(int i = 0; i < x.length; ++i){
            y[i] = x[i] * std[i] + mu[i];
        }
        return y;
    }

    public double[] standardize(double[] x){
        double[] y = new double[x.length];
        for(int i = 0; i < x.length; ++i){
            y[i] = (x[i] - mu[i]) / std[i];
        }
        return y;
    }

    protected void query(List<double[]> batch){
        int dimension = batch.get(0).length;
        int m = batch.size();

        //normalization
        mu = new double[dimension];
        std = new double[dimension];
        for (int i = 0; i < dimension; ++i) {
            mu[i] = 0;
            std[i] = 0;
        }

        for (int i = 0; i < m; ++i) {
            double[] x = batch.get(i);
            for (int j = 0; j < dimension; ++j) {
                mu[j] += x[j];
            }
        }

        for (int i = 0; i < dimension; ++i) {
            mu[i] /= m;
        }

        for (int i = 0; i < m; ++i) {
            double[] x = batch.get(i);
            for (int j = 0; j < dimension; ++j) {
                std[j] += Math.pow(x[j] - mu[j], 2);
            }
        }

        for (int i = 0; i < dimension; ++i) {
            std[i] /= (m - 1);
        }

        for (int i = 0; i < dimension; ++i) {
            std[i] = Math.sqrt(std[i]);
        }
    }

    protected void query(IntelliContext batch) {

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;
        int m = batch.tupleCount();

        //normalization
        mu = new double[dimension];
        std = new double[dimension];
        for (int i = 0; i < dimension; ++i) {
            mu[i] = 0;
            std[i] = 0;
        }

        for (int i = 0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            for (int j = 0; j < dimension; ++j) {
                mu[j] += x[j];
            }
        }

        for (int i = 0; i < dimension; ++i) {
            mu[i] /= m;
        }

        for (int i = 0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            for (int j = 0; j < dimension; ++j) {
                std[j] += Math.pow(x[j] - mu[j], 2);
            }
        }

        for (int i = 0; i < dimension; ++i) {
            std[i] /= (m - 1);
        }

        for (int i = 0; i < dimension; ++i) {
            std[i] = Math.sqrt(std[i]);
        }

    }
}
