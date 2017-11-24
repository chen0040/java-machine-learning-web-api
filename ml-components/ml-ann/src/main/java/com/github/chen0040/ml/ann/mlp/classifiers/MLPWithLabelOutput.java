package com.github.chen0040.ml.ann.mlp.classifiers;

import com.github.chen0040.ml.ann.mlp.MLPNet;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.ann.mlp.MLP;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by memeanalytics on 5/9/15.
 */
public class MLPWithLabelOutput extends MLP {
    public Supplier<List<String>> classLabelsModel;

    @Override
    public boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public double[] getTarget(IntelliTuple tuple) {
        List<String> labels = classLabelsModel.get();
        double[] target = new double[labels.size()];
        for (int i = 0; i < labels.size(); ++i) {
            target[i] = labels.get(i).equals(tuple.getLabelOutput()) ? 1 : 0;
        }
        return target;
    }

    @Override
    public Object clone(){
        MLPWithLabelOutput clone = new MLPWithLabelOutput();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(MLPNet rhs){
        super.copy(rhs);

        MLPWithLabelOutput rhs2 = (MLPWithLabelOutput)rhs;
        classLabelsModel = rhs2.classLabelsModel;
    }
}
