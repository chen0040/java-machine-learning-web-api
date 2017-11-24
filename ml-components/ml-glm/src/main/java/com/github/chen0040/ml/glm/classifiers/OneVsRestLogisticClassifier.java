package com.github.chen0040.ml.glm.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.glm.binaryclassifiers.LogisticBinaryClassifier;
import com.github.chen0040.ml.commons.classifiers.AbstractOneVsRestClassifier;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;

import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class OneVsRestLogisticClassifier extends AbstractOneVsRestClassifier {

    @Override
    public Object clone(){
        OneVsRestLogisticClassifier clone = new OneVsRestLogisticClassifier();
        clone.copy(this);

        return clone;
    }

    @Override
    protected BinaryClassifier createClassifier(String classLabel){
        return new LogisticBinaryClassifier(classLabel);
    }

    public OneVsRestLogisticClassifier(List<String> classLabels){
        super(classLabels);
    }

    public OneVsRestLogisticClassifier(){
        super();
    }

    @Override
    protected double getClassifierScore(IntelliTuple tuple, String classLabel){
        BinaryClassifier classifier = getClassifier(classLabel);
        double score = classifier.evaluate(tuple, classifier.getModelSource());
        return score;
    }
}
