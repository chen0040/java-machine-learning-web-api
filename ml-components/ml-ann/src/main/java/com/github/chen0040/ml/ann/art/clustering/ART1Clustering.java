package com.github.chen0040.ml.ann.art.clustering;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.dataprepare.transform.Standardization;
import com.github.chen0040.ml.ann.art.core.ART1;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class ART1Clustering extends AbstractClustering {
    private ART1 net;
    private int initialNodeCount;
    private boolean allowNewNodeInPrediction;
    private Standardization inputNormalization;

    public static final String ALPHA = "QAlpha";
    public static final String BETA = "beta";
    public static final String RHO = "rho";

    private double alpha() // choice parameter
    {
        return getAttribute(ALPHA);
    }

    private double beta() // learning rate
    {
        return getAttribute(BETA);
    }

    private double rho() // resonance threshold
    {
        return getAttribute(RHO);
    }

    private void _alpha(double alpha){
        setAttribute(ALPHA, alpha);
    }

    private void _beta(double beta){
        setAttribute(BETA, beta);
    }

    private void _rho(double rho){
        setAttribute(RHO, rho);
    }

    @Override
    public Object clone(){
        ART1Clustering clone = new ART1Clustering();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        ART1Clustering rhs2 = (ART1Clustering)rhs;
        net = rhs2.net == null ? null : (ART1)rhs2.net.clone();
        initialNodeCount = rhs2.initialNodeCount;
        allowNewNodeInPrediction = rhs2.allowNewNodeInPrediction;
        inputNormalization = rhs2.inputNormalization == null ? null : (Standardization)rhs2.inputNormalization.clone();
    }

    public ART1Clustering(){
        _rho(0.9);
        _beta(0.3);
        _alpha(0.1);
        initialNodeCount = 1;
        allowNewNodeInPrediction = false;
    }

    public boolean allowNewNodeInPrediction() {
        return allowNewNodeInPrediction;
    }

    public void setAllowNewNodeInPrediction(boolean allowNewNodeInPrediction) {
        this.allowNewNodeInPrediction = allowNewNodeInPrediction;
    }

    public int getInitialNodeCount() {
        return initialNodeCount;
    }

    public void setInitialNodeCount(int initialNodeCount) {
        this.initialNodeCount = initialNodeCount;
    }

    @Override
    public int getCluster(IntelliTuple tuple) {
        return simulate(tuple, allowNewNodeInPrediction);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;
        inputNormalization = new Standardization(batch);

        net=new ART1(dimension, initialNodeCount);
        net.alpha = alpha();
        net.beta = beta();
        net.rho = rho();

        int m = batch.tupleCount();
        for(int i=0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            simulate(tuple, true);
        }

        return new BatchUpdateResult();
    }

    public int simulate(IntelliTuple tuple, boolean can_create_new_node){
        double[] x = getModelSource().toNumericArray(tuple);
        x = inputNormalization.standardize(x);
        double[] y = binarize(x);
        int clusterId = net.simulate(y, can_create_new_node);
        tuple.setPredictedLabelOutput(String.format("%d", clusterId));
        return clusterId;
    }

    private double[] binarize(double[] x){
        double[] y = new double[x.length];
        for(int i=0; i < x.length; ++i){
            if(x[i] > 0){
                y[i] = 1;
            }else{
                y[i] = 0;
            }
        }

        return y;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
