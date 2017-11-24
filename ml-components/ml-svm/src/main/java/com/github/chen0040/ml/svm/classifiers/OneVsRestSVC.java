package com.github.chen0040.ml.svm.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.svm.binaryclassifiers.SVC;
import com.github.chen0040.ml.commons.classifiers.AbstractOneVsRestClassifier;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;

import java.util.List;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class OneVsRestSVC extends AbstractOneVsRestClassifier {

    @Override
    public Object clone(){
        OneVsRestSVC clone = new OneVsRestSVC();
        clone.copy(this);
        return clone;
    }

    @Override
    protected BinaryClassifier createClassifier(String classLabel){
        return new SVC(classLabel);
    }

    public OneVsRestSVC(List<String> classLabels){
        super(classLabels);
    }

    public OneVsRestSVC(){
        super();
    }

    @Override
    protected double getClassifierScore(IntelliTuple tuple, String classLabel){
        BinaryClassifier classifier = getClassifier(classLabel);
        double score = classifier.evaluate(tuple, getModelSource());
        return score;
    }
}
