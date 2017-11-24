package com.github.chen0040.ml.commons.regressions;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;

/**
 * Created by memeanalytics on 15/8/15.
 */
public interface Regression extends MLModule {
    PredictionResult predict(IntelliTuple tuple);
}
