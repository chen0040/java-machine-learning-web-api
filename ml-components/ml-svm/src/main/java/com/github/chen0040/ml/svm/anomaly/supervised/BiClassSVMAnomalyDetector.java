package com.github.chen0040.ml.svm.anomaly.supervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.AbstractBinaryClassifier;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.svm.smo.SMOSVC;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.svm.smo.kernels.GaussianKernelFunction;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class BiClassSVMAnomalyDetector extends AbstractAnomalyDetecter {
    private BinaryClassifier classifier;

    @Override
    public void copy(MLModule rhs)
    {
        super.copy(rhs);

        BiClassSVMAnomalyDetector rhs2 = (BiClassSVMAnomalyDetector)rhs;
        AbstractBinaryClassifier abc2 = (AbstractBinaryClassifier)rhs2.classifier;
        classifier = abc2 == null ? null : (BinaryClassifier)abc2.clone();
    }

    @Override
    public Object clone(){
        BiClassSVMAnomalyDetector clone = new BiClassSVMAnomalyDetector();
        clone.copy(this);

        return clone;
    }

    public BiClassSVMAnomalyDetector(){
        SMOSVC svc = new SMOSVC(AnomalyClassLabels.IS_ANOMALY);
        svc.setKernelFunction(new GaussianKernelFunction());
        classifier = svc;
    }

    public void setClassifier(BinaryClassifier classifier){
        this.classifier = classifier;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        return classifier.evaluate(tuple, context);
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        return classifier.isInClass(tuple, getModelSource());
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        return classifier.batchUpdate(batch);
    }
}
