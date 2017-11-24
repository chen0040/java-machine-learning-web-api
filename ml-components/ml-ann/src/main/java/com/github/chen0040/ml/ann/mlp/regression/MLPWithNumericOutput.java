package com.github.chen0040.ml.ann.mlp.regression;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.ann.mlp.MLP;

/**
 * Created by memeanalytics on 5/9/15.
 */
public class MLPWithNumericOutput extends MLP {

    @Override
    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasNumericOutput();
    }

    @Override
    public double[] getTarget(IntelliTuple tuple) {
        double[] target = new double[1];
        target[0] = tuple.getNumericOutput();
        return target;
    }

    @Override
    public Object clone(){
        MLPWithNumericOutput clone = new MLPWithNumericOutput();
        clone.copy(this);

        return clone;
    }
}
