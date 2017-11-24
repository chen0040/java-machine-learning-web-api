package com.github.chen0040.ml.glm.anomaly;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.glm.binaryclassifiers.LogisticBinaryClassifier;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class LogisticAnomalyDetector extends AbstractAnomalyDetecter {
    private LogisticBinaryClassifier classifier;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LogisticAnomalyDetector rhs2 = (LogisticAnomalyDetector)rhs;
        classifier = rhs2.classifier == null ? null : (LogisticBinaryClassifier)rhs2.classifier.clone();
    }

    @Override
    public Object clone(){
        LogisticAnomalyDetector clone = new LogisticAnomalyDetector();
        clone.copy(this);
        return clone;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        return classifier.isInClass(tuple, classifier.getModelSource());
    }

    public LogisticAnomalyDetector(){
        super();
        classifier = new LogisticBinaryClassifier(AnomalyClassLabels.IS_ANOMALY);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        return classifier.batchUpdate(batch);
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        return classifier.evaluate(tuple, context);
    }

    public void setThreshold(double threshold){
        classifier.setThreshold(threshold);
    }

}
