package com.github.chen0040.ml.spark.core.clustering;


import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Created by memeanalytics on 17/8/15.
 */
public interface Clustering extends SparkMLModule {
    int getCluster(JavaSparkContext context, SparkMLTuple tuple);
    int getClusterOfClosestTuple(JavaSparkContext context, SparkMLTuple tuple, JavaRDD<SparkMLTuple> batch);
    double computeDBI(JavaSparkContext context, JavaRDD<SparkMLTuple> batch);
}
