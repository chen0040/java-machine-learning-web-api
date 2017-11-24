package com.github.chen0040.ml.commons.anomaly;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;

/**
 * Created by memeanalytics on 15/8/15.
 */
public interface AnomalyDetector extends MLModule {
    boolean isAnomaly(IntelliTuple tuple);
    double evaluate(IntelliTuple tupe, IntelliContext context);
}
