package com.github.chen0040.ml.commons.discrete;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface AttributeValueDiscretizer extends MLModule {
    int discretize(double value, int index);
    IntelliTuple discretize(IntelliTuple tuple);
    IntelliContext discretize(IntelliContext batch);
}
