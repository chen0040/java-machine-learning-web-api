package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;

/**
 * Created by memeanalytics on 13/8/15.
 */
public interface BinaryClassifier extends MLModule {
    boolean isInClass(IntelliTuple tuple, IntelliContext context);
    String getPositiveClassLabel();
    void setPositiveClassLabel(String label);
    String getNegativeClassLabel();
    void setNegativeClassLabel(String label);
    double computePredictionAccuracy(IntelliContext batch);
}
