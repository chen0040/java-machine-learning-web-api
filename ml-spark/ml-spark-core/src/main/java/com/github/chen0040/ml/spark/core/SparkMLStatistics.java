package com.github.chen0040.ml.spark.core;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;

import java.io.Serializable;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class SparkMLStatistics implements Serializable {
    
    public static double[] maxByFeature(JavaRDD<SparkMLTuple> dataset){
        SparkMLTuple max = dataset.reduce(new Function2<SparkMLTuple, SparkMLTuple, SparkMLTuple>() {
            public SparkMLTuple call(SparkMLTuple t1, SparkMLTuple t2) throws Exception {
                int dimension = t1.getDimension();
                SparkMLTuple result = (SparkMLTuple)t1.clone();
                for(int i=0; i < dimension; ++i){
                    boolean t1_valid = t1.containsKey(i);
                    boolean t2_valid = t2.containsKey(i);
                    if(t1_valid && t2_valid){
                        result.put(i, Math.max(t1.get(i), t2.get(i)));
                    } else if(t2_valid){
                        result.put(i, t2.get(i));
                    }
                }
                return result;
            }
        });


        int dimension = max.getDimension();
        double[] maxVector = new double[dimension];
        for(int i=0; i < dimension; ++i){
            if(max.containsKey(i)) {
                maxVector[i] = max.get(i);
            }
        }

        return maxVector;
    }

    public static double[] minByFeature(JavaRDD<SparkMLTuple> dataset){
        SparkMLTuple min = dataset.reduce(new Function2<SparkMLTuple, SparkMLTuple, SparkMLTuple>() {
            public SparkMLTuple call(SparkMLTuple t1, SparkMLTuple t2) throws Exception {
                int dimension = t1.getDimension();
                SparkMLTuple result = (SparkMLTuple)t1.clone();
                for(int i=0; i < dimension; ++i){
                    boolean t1_valid = t1.containsKey(i);
                    boolean t2_valid = t2.containsKey(i);
                    if(t1_valid && t2_valid){
                        result.put(i, Math.min(t1.get(i), t2.get(i)));
                    } else if(t2_valid){
                        result.put(i, t2.get(i));
                    }
                }
                return result;
            }
        });


        int dimension = min.getDimension();
        double[] minVector = new double[dimension];
        for(int i=0; i < dimension; ++i){
            if(min.containsKey(i)) {
                minVector[i] = min.get(i);
            }
        }

        return minVector;
    }
}
