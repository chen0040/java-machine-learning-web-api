package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;

/**
 * Created by memeanalytics on 16/8/15.
 */
public interface Classifier extends SparkMLModule {
    String predict(SparkMLTuple tuple);
}
