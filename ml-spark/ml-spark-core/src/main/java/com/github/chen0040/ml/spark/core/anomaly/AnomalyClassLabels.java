package com.github.chen0040.ml.spark.core.anomaly;

import com.github.chen0040.ml.spark.core.SparkMLTuple;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class AnomalyClassLabels {
    public static final String IS_ANOMALY = "IS_ANOMALY";
    public static final String IS_NOT_ANOMALY = "IS_NOT_ANOMALY";
    public static boolean isAnomaly(SparkMLTuple tuple){
        return IS_ANOMALY.equals(tuple.getLabelOutput());
    }
}
