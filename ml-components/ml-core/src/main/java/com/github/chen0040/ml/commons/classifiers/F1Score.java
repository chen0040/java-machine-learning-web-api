package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.anomaly.AnomalyDetector;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class F1Score {
    public static F1ScoreResult score(BinaryClassifier algorithm, IntelliContext crossValidationData) {

        int true_positive = 0;
        int false_positive = 0;
        int false_negative = 0;

        int n = crossValidationData.tupleCount();

        for(int i=0; i < n; ++i){
            IntelliTuple tuple = crossValidationData.tupleAtIndex(i);
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
    }

    public static F1ScoreResult score(AnomalyDetector algorithm, IntelliContext crossValidationData) {

        int true_positive = 0;
        int false_positive = 0;
        int false_negative = 0;

        int n = crossValidationData.tupleCount();

        for(int i=0; i < n; ++i){
            IntelliTuple tuple = crossValidationData.tupleAtIndex(i);
            boolean predicted_positive = algorithm.isAnomaly(tuple);
            boolean is_positive = AnomalyClassLabels.isAnomaly(tuple);
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
    }

    public static void score(BinaryClassifier algorithm, IntelliContext crossValidationData, Consumer<F1ScoreResult> onCompleted){

        F1ScoreResult result = score(algorithm, crossValidationData);

        if(onCompleted != null){
            onCompleted.accept(result);
        }
    }

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
