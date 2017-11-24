package com.github.chen0040.ml.spark.core.discrete;

import org.apache.spark.api.java.JavaRDD;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface DiscreteFilter {
    void build(JavaRDD<Double> rdd);
    int discretize(double value);
}
