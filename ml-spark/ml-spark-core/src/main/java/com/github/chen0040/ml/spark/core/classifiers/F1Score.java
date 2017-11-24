package com.github.chen0040.ml.spark.core.classifiers;


import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.anomaly.AnomalyDetector;
import com.github.chen0040.ml.spark.core.anomaly.AnomalyClassLabels;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class F1Score {

    /*
    public static F1ScoreResult score(BinaryClassifier algorithm, JavaRDD<SparkMLTuple> crossValidationData) {

        int true_positive = 0;
        int false_positive = 0;
        int false_negative = 0;

        int n = crossValidationData.size();

        for(int i=0; i < n; ++i){
            Tuple tuple = crossValidationData.tupleAtIndex(i);
            boolean predicted_positive = algorithm.isInClass(tuple, crossValidationData);
            boolean is_positive = BinaryClassifierUtils.isInClass(tuple, algorithm.getPositiveClassLabel());
            if(predicted_positive){
                if(is_positive){
                    true_positive++;
                }else{
                    false_positive++;
                }
            }else{
                if(is_positive) false_negative++;
            }
        }

        double precision = (double)true_positive / (true_positive + false_positive);
        double recall = (double)true_positive / (true_positive + false_negative);
        double f1score = precision * recall * 2/ (precision + recall);

        return new F1ScoreResult(f1score, precision, recall);
    }*/

    public static F1ScoreResult score(final AnomalyDetector algorithm, final JavaRDD<SparkMLTuple> crossValidationData) {

        final int true_positive_type = 1;
        final int true_negative_type = 2;
        final int false_positive_type = 3;
        final int false_negative_type = 4;



        JavaPairRDD<Integer, Integer> rdd = crossValidationData.mapToPair(new PairFunction<SparkMLTuple, Integer, Integer>() {
            public Tuple2<Integer, Integer> call(SparkMLTuple tuple) throws Exception {
                boolean predicted_positive = algorithm.isAnomaly(tuple);
                boolean is_positive = AnomalyClassLabels.isAnomaly(tuple);
                int type = -1;
                if (predicted_positive) {
                    if (is_positive) {
                        type = true_positive_type;
                    } else {
                        type = false_positive_type;
                    }
                } else {
                    if (is_positive) type = false_negative_type;
                    else type = true_negative_type;
                }
                return new Tuple2<Integer, Integer>(type, 1);
            }
        });

        JavaPairRDD<Integer, Integer> rdd2 = rdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        Map<Integer, Integer> typeCounts = rdd2.collectAsMap();

        int true_positive = typeCounts.getOrDefault(true_positive_type, 0);
        int true_negative = typeCounts.getOrDefault(true_negative_type, 0);
        int false_positive = typeCounts.getOrDefault(false_positive_type, 0);
        int false_negative = typeCounts.getOrDefault(false_negative_type, 0);

        double precision = (double)true_positive / (true_positive + false_positive);
        double recall = (double)true_positive / (true_positive + false_negative);
        double f1score = precision * recall * 2/ (precision + recall);

        return new F1ScoreResult(f1score, precision, recall);
    }

    /*
    public static void score(BinaryClassifier algorithm, JavaRDD<SparkMLTuple> crossValidationData, Consumer<F1ScoreResult> onCompleted){

        F1ScoreResult result = score(algorithm, crossValidationData);

        if(onCompleted != null){
            onCompleted.accept(result);
        }
    }*/

    public static double score(List<Integer> expected_outlier_index_list, List<Integer> predicted_outlier_index_list, int list_length){

        int true_positive = 0;
        int false_positive = 0;
        int false_negative = 0;

        for(int i=0; i < list_length; ++i){
            boolean predicted_positive = predicted_outlier_index_list.contains(i);
            boolean is_positive = expected_outlier_index_list.contains(i);

            if(predicted_positive){
                if(is_positive){
                    true_positive++;
                }else{
                    false_positive++;
                }
            }else{
                if(is_positive) false_negative++;
            }
        }

        double precision = (double)true_positive / (true_positive + false_positive);
        double recall = (double)true_positive / (true_positive + false_negative);
        double f1score = precision * recall * 2/ (precision + recall);

        return f1score;
    }
}
