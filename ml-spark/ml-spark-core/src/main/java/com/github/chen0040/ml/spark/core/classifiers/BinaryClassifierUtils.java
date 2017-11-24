package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class BinaryClassifierUtils {

    public static boolean isInClass(SparkMLTuple tuple, String classLabel){
        return classLabel.equals(tuple.getLabelOutput());
    }
}
