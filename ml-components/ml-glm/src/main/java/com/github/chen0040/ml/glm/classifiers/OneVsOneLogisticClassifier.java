package com.github.chen0040.ml.glm.classifiers;

import com.github.chen0040.ml.commons.classifiers.AbstractOneVsOneClassifier;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.glm.binaryclassifiers.LogisticBinaryClassifier;

import java.util.List;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public class OneVsOneLogisticClassifier extends AbstractOneVsOneClassifier {

    @Override
    public Object clone(){
        OneVsOneLogisticClassifier clone = new OneVsOneLogisticClassifier();
        clone.copy(this);

        return clone;
    }

    @Override
    protected BinaryClassifier createClassifier(String classLabel){
        return new LogisticBinaryClassifier(classLabel);
    }

    public OneVsOneLogisticClassifier(List<String> classLabels){
        super(classLabels);
    }

    public OneVsOneLogisticClassifier(){
        super();
    }

    @Override
    protected double getClassifierScore(IntelliTuple tuple, BinaryClassifier classifier){
        double score = classifier.evaluate(tuple, classifier.getModelSource());
        return score;
    }
}
