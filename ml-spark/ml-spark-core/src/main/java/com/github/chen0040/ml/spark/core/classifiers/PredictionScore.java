package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class PredictionScore {

    public static double score(BinaryClassifier classifier, JavaRDD<SparkMLTuple> Xval){
        return score(classifier, Xval, false);
    }

    public static double score(BinaryClassifier classifier, JavaRDD<SparkMLTuple> Xval, boolean verbal)
    {
        JavaRDD<Tuple2<Integer, Integer>> rdd = Xval.map(new Function<SparkMLTuple, Tuple2<Integer, Integer>>() {
            public Tuple2<Integer, Integer> call(SparkMLTuple tuple) throws Exception {
                String label = tuple.getLabelOutput();
                if (label == null) return new Tuple2<Integer, Integer>(0, 1);
                return new Tuple2<Integer, Integer>(label.equals(tuple.getPredictedLabelOutput()) ? 1 : 0, 1);
            }
        });

        Tuple2<Integer, Integer> output = rdd.reduce(new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
            public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> t1, Tuple2<Integer, Integer> t2) throws Exception {
                return new Tuple2<Integer, Integer>(t1._1 + t2._1, t1._2 + t2._2);
            }
        });

        int correctnessCount = output._1;
        int m = output._2;

        return (double)correctnessCount / m;
    }


}
