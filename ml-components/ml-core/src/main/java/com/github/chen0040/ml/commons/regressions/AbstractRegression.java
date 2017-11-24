package com.github.chen0040.ml.commons.regressions;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.MLModuleOutputType;

/**
 * Created by memeanalytics on 16/8/15.
 */
public abstract  class AbstractRegression extends AbstractMLModule implements Regression {

    public AbstractRegression(){
        super();
        setOutputType(MLModuleOutputType.PredictedOutputs);
    }

    public abstract PredictionResult predict(IntelliTuple tuple) ;
}
