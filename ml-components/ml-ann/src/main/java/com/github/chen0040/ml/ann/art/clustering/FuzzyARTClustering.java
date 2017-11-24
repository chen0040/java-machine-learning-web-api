package com.github.chen0040.ml.ann.art.clustering;

import com.github.chen0040.ml.ann.art.core.FuzzyART;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.dataprepare.transform.ComplementaryCoding;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class FuzzyARTClustering extends AbstractClustering {
    private FuzzyART net;
    private int initialNodeCount;
    private boolean allowNewNodeInPrediction;
    private ComplementaryCoding inputNormalization;

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
    public void copy(MLModule rhs){
        super.copy(rhs);

        FuzzyARTClustering rhs2 = (FuzzyARTClustering)rhs;
        net = rhs2.net == null ? null : (FuzzyART)rhs2.net.clone();
        initialNodeCount = rhs2.initialNodeCount;
        allowNewNodeInPrediction = rhs2.allowNewNodeInPrediction;
        inputNormalization = rhs2.inputNormalization == null ? null : (ComplementaryCoding)rhs2.inputNormalization.clone();
    }

    @Override
    public Object clone(){
        FuzzyARTClustering clone = new FuzzyARTClustering();
        clone.copy(this);

        return clone;
    }

    public FuzzyARTClustering(){
        _rho(0.7);
        _beta(0.2);
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


        inputNormalization = new ComplementaryCoding(batch);
        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length * 2; // times 2 due to complementary coding

        net=new FuzzyART(dimension, initialNodeCount);
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

    public int simulate(IntelliTuple tuple, boolean can_create_node){
        double[] x = getModelSource().toNumericArray(tuple);
        x = inputNormalization.normalize(x);
        return net.simulate(x, can_create_node);
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
