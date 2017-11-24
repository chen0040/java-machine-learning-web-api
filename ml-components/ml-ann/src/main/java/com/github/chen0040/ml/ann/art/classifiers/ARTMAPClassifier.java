package com.github.chen0040.ml.ann.art.classifiers;

import com.github.chen0040.ml.ann.art.core.ARTMAP;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.AbstractClassifier;
import com.github.chen0040.ml.dataprepare.transform.ComplementaryCoding;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.function.Function;

/**
 * Created by memeanalytics on 23/8/15.
 */
public class ARTMAPClassifier extends AbstractClassifier {

    private ARTMAP net;

    public static final String ALPHA = "QAlpha";
    public static final String BETA = "beta";
    public static final String RHO_BASE = "rho baseline";

    private boolean allowNewNodeInPrediction;
    private ComplementaryCoding inputNormalization;

    private double alpha() // choice parameter
    {
        return getAttribute(ALPHA);
    }
    private void _alpha(double alpha){
        setAttribute(ALPHA, alpha);
    }
    private double beta() // learning rate
    {
        return getAttribute(BETA);
    }
    private void _beta(double beta){
        setAttribute(BETA, beta);
    }
    private double rho_baseline() // resonance vigilance value
    {
        return getAttribute(RHO_BASE);
    }
    private void _rho_baseline(double rho){
        setAttribute(RHO_BASE, rho);
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        ARTMAPClassifier rhs2 = (ARTMAPClassifier)rhs;
        net = rhs2.net == null ? null : (ARTMAP)rhs2.net.clone();
        allowNewNodeInPrediction = rhs2.allowNewNodeInPrediction;
        inputNormalization = rhs2.inputNormalization == null ? null : (ComplementaryCoding)rhs2.inputNormalization.clone();
    }

    @Override
    public Object clone(){
        ARTMAPClassifier clone = new ARTMAPClassifier();
        clone.copy(this);

        return clone;
    }

    public ARTMAPClassifier(){
        _rho_baseline(0.1);
        _beta(0.3);
        _alpha(0.1);
        allowNewNodeInPrediction = false;
    }

    public boolean allowNewNodeInPrediction() {
        return allowNewNodeInPrediction;
    }

    public void setAllowNewNodeInPrediction(boolean allowNewNodeInPrediction) {
        this.allowNewNodeInPrediction = allowNewNodeInPrediction;
    }

    @Override
    public String predict(IntelliTuple tuple) {
        return simulate(tuple, false);
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        inputNormalization = new ComplementaryCoding(batch);
        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length * 2; // times 2 due to complementary coding

        net=new ARTMAP(dimension);
        net.alpha = alpha();
        net.beta = beta();
        net.rho = rho_baseline();

        int m = batch.tupleCount();
        for(int i=0; i < m; ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            simulate(tuple, true);
        }

        return new BatchUpdateResult();
    }

    public String simulate(IntelliTuple tuple, boolean can_create_node){
        double[] x = getModelSource().toNumericArray(tuple);
        x = inputNormalization.normalize(x);
        return net.simulate(x, tuple.getLabelOutput(), can_create_node);
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
    public int nodeCount() {
        return net.getNodeCount();
    }

}
