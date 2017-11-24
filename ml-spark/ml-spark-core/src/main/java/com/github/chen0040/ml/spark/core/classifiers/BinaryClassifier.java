package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaRDD;

/**
 * Created by memeanalytics on 13/8/15.
 */
public interface BinaryClassifier extends SparkMLModule {
    boolean isInClass(SparkMLTuple tuple);
    String getPositiveClassLabel();
    void setPositiveClassLabel(String label);
    String getNegativeClassLabel();
    void setNegativeClassLabel(String label);
    double computePredictionAccuracy(JavaRDD<SparkMLTuple> batch);
}
