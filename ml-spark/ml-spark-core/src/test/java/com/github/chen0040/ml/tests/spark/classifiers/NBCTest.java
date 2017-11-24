package com.github.chen0040.ml.tests.spark.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.spark.bayes.NBC;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.testng.annotations.Test;
import scala.Tuple2;

/**
 * Created by root on 9/16/15.
 */
public class NBCTest extends MLearnTestCase {
    private static NBC method;
    private static JavaRDD<SparkMLTuple> batch;

    public NBCTest(){
        super("NBC");
    }

    @Override
    public void setup(){
        super.setup();
        method = new NBC();
    }


    @Test
    public void TestHeartScale(){
        batch = readHeartScale();


        method.batchUpdate(batch);

        double predictionAccuracy = computePredictionAccuracy();

        System.out.println("Prediction Accuracy: " + predictionAccuracy);
    }

    private static Function<SparkMLTuple, Tuple2<Integer, Integer>> countCorrectness = new Function<SparkMLTuple, Tuple2<Integer, Integer>>() {
        public Tuple2<Integer, Integer> call(SparkMLTuple tuple) throws Exception {
            String label = tuple.getLabelOutput();
            if (label == null) return new Tuple2<Integer, Integer>(0, 1);
            String predictedLabel = method.predict(tuple);
            //System.out.println(label + " : " + predictedLabel);
            return new Tuple2<Integer, Integer>(label.equals(predictedLabel) ? 1 : 0, 1);
        }
    };

    private static Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> reduceCorrectnessStats = new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
        public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> t1, Tuple2<Integer, Integer> t2) throws Exception {
            return new Tuple2<Integer, Integer>(t1._1 + t2._1, t1._2 + t2._2);
        }
    };

    public double computePredictionAccuracy(){

        JavaRDD<Tuple2<Integer, Integer>> rdd = batch.map(countCorrectness);

        Tuple2<Integer, Integer> output = rdd.reduce(reduceCorrectnessStats);

        int correctnessCount = output._1;
        int m = output._2;

        return (double)correctnessCount / m;
    }


}
