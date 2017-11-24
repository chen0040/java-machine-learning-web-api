package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;

/**
 * Created by memeanalytics on 16/8/15.
 */
public interface Classifier extends MLModule {
    String predict(IntelliTuple tuple);
    double computePredictionAccuracy(IntelliContext batch);
}
