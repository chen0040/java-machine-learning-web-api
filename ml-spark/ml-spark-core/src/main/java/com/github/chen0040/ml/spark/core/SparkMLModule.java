package com.github.chen0040.ml.spark.core;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;

/**
 * Created by memeanalytics on 13/9/15.
 */
public interface SparkMLModule extends Serializable {



    String getPrototype();

    SparkMLOutputType getOutputType();
    void setOutputType(SparkMLOutputType outputType);

    String getModelSourceId();
    void setModelSourceId(String modelSourceId);

    boolean getIsLabelRequiredInBatchUpdate();
    void setIsLabelRequiredInBatchUpdate(boolean isReq);

    void copy(SparkMLModule rhs);

    BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch);
    PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc);
    PredictionResult evaluate(SparkMLTuple tuple);
}
