package com.github.chen0040.ml.spark.core.anomaly;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;

/**
 * Created by memeanalytics on 15/8/15.
 */
public interface AnomalyDetector extends SparkMLModule {
    boolean isAnomaly(SparkMLTuple tuple);
}
