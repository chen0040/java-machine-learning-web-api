package com.github.chen0040.ml.commons.anomaly;

import com.github.chen0040.ml.commons.IntelliTuple;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class AnomalyClassLabels {
    public static final String IS_ANOMALY = "IS_ANOMALY";
    public static final String IS_NOT_ANOMALY = "IS_NOT_ANOMALY";
    public static boolean isAnomaly(IntelliTuple tuple){
        return IS_ANOMALY.equals(tuple.getLabelOutput());
    }
}
