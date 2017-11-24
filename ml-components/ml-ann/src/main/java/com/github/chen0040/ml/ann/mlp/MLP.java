package com.github.chen0040.ml.ann.mlp;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.dataprepare.transform.Standardization;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 21/8/15.
 */
public abstract class MLP extends MLPNet {
    private Standardization inputNormalization;
    private Standardization outputNormalization;

    private boolean normalizeOutputs;


    public void copy(MLPNet rhs){
        super.copy(rhs);

        MLP rhs2 = (MLP)rhs;
        inputNormalization = rhs2.inputNormalization == null ? null : (Standardization)rhs2.inputNormalization.clone();
        outputNormalization = rhs2.outputNormalization == null ? null : (Standardization)rhs2.outputNormalization.clone();
        normalizeOutputs = rhs2.normalizeOutputs;
    }

    public MLP(){
        super();
        normalizeOutputs = false;
    }

    protected abstract boolean isValidTrainingSample(IntelliTuple tuple);

    public void setNormalizeOutputs(boolean normalize){
        this.normalizeOutputs = normalize;
    }

    public abstract double[] getTarget(IntelliTuple tuple);


    public void train(IntelliContext batch, int training_epoches)
    {
        inputNormalization = new Standardization(batch);


        if(normalizeOutputs) {
            List<double[]> targets = new ArrayList<double[]>();
            for(int i = 0; i < batch.tupleCount(); ++i){
                IntelliTuple tuple = batch.tupleAtIndex(i);
                if(isValidTrainingSample(tuple)) {
                    double[] target = getTarget(tuple);
                    targets.add(target);
                }
            }
            outputNormalization = new Standardization(targets);
        }

        for(int count=0; count<training_epoches; ++count)
        {
            for(int i = 0; i<batch.tupleCount(); i++)
            {
                IntelliTuple tuple = batch.tupleAtIndex(i);
                if(isValidTrainingSample(tuple)) {
                    double[] x = batch.toNumericArray(tuple);
                    x = inputNormalization.standardize(x);

                    double[] target = getTarget(tuple);

                    if (outputNormalization != null) {
                        target = outputNormalization.standardize(target);
                    }

                    train(x, target);
                }
            }
        }
    }

    public double[] predict(IntelliContext context, IntelliTuple tuple){

        double[] x = context.toNumericArray(tuple);
        x = inputNormalization.standardize(x);

        double[] target = predict(x);

        if(outputNormalization != null){
            target = outputNormalization.revert(target);
        }

        return target;
    }
}
