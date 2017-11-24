package com.github.chen0040.ml.spark.core.discrete;


import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaRDD;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface AttributeValueDiscretizer extends SparkMLModule {
    int discretize(double value, int index);
    SparkMLTuple discretize(SparkMLTuple tuple);
    JavaRDD<SparkMLTuple> discretize(JavaRDD<SparkMLTuple> batch);
    void makeColumnDiscrete(int columnIndex);
    boolean coverColumn(int columnIndex);
}
