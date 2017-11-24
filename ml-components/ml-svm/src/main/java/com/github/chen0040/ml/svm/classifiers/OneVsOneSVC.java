package com.github.chen0040.ml.svm.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.svm.binaryclassifiers.SVC;
import com.github.chen0040.ml.commons.classifiers.AbstractOneVsOneClassifier;

import java.util.List;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public class OneVsOneSVC extends AbstractOneVsOneClassifier {

    @Override
    public Object clone(){
        OneVsOneSVC clone = new OneVsOneSVC();
        clone.copy(this);
        return clone;
    }

    public OneVsOneSVC(List<String> classLabels){
        super(classLabels);
    }

    public OneVsOneSVC(){
        super();
    }

    @Override
    protected BinaryClassifier createClassifier(String classLabel){
        return new SVC(classLabel);
    }

    @Override
    protected double getClassifierScore(IntelliTuple tuple, BinaryClassifier classifier){
        double h = classifier.evaluate(tuple, classifier.getModelSource());
        return h;
    }
}
