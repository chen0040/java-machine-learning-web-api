package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 15/8/15.
 */
public abstract class AbstractBinaryClassifier extends AbstractSparkMLModule implements  BinaryClassifier {
    private String positiveClassLabel;
    private String negativeClassLabel;

    public double computePredictionAccuracy(JavaRDD<SparkMLTuple> batch){
        JavaRDD<Tuple2<Integer, Integer>> rdd = batch.map(new Function<SparkMLTuple, Tuple2<Integer, Integer>>() {
            public Tuple2<Integer, Integer> call(SparkMLTuple tuple) throws Exception {
                String label = tuple.getLabelOutput();
                if (label == null) return new Tuple2<Integer, Integer>(0, 1);
                return new Tuple2<Integer, Integer>(label.equals(tuple.getPredictedLabelOutput()) ? 1 : 0, 1);
            }
        });

        Tuple2<Integer, Integer> output = rdd.reduce(new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
            public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> t1, Tuple2<Integer, Integer> t2) throws Exception {
                return new Tuple2<Integer, Integer>(t1._1+t2._1, t1._2 + t2._2);
            }
        });

        int correctnessCount = output._1;
        int m = output._2;

        return (double)correctnessCount / m;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        AbstractBinaryClassifier rhs2 = (AbstractBinaryClassifier)rhs;
        positiveClassLabel = rhs2.positiveClassLabel;
        negativeClassLabel = rhs2.negativeClassLabel;
    }

    public AbstractBinaryClassifier(String classLabel){
        this.positiveClassLabel = classLabel;
        setIsLabelRequiredInBatchUpdate(true);
    }
    public AbstractBinaryClassifier(){
        setIsLabelRequiredInBatchUpdate(true);
    }

    public String getPositiveClassLabel(){
        return positiveClassLabel;
    }

    public void setPositiveClassLabel(String label){
        positiveClassLabel = label;
    }

    public String getNegativeClassLabel(){
        if(negativeClassLabel==null){
            return String.format("NOT(%s)", getPositiveClassLabel());
        }
        return negativeClassLabel;
    }

    public void setNegativeClassLabel(String negativeClassLabel){
        this.negativeClassLabel = negativeClassLabel;
    }

    public abstract boolean isInClass(SparkMLTuple tuple);

    protected void scan4ClassLabel(JavaRDD<SparkMLTuple> batch){
        JavaPairRDD<String, Integer> rdd = batch.mapToPair(new PairFunction<SparkMLTuple, String, Integer>() {
            public Tuple2<String, Integer> call(SparkMLTuple tuple) throws Exception {
                return new Tuple2<String, Integer>(tuple.getLabelOutput(), 1);
            }
        });

        JavaPairRDD<String, Integer> rdd1 = rdd.filter(new Function<Tuple2<String, Integer>, Boolean>() {
            public Boolean call(Tuple2<String, Integer> s) throws Exception {
                return s._1 != null;
            }
        });

        JavaPairRDD<String, Integer> rdd2 = rdd1.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = rdd2.collect();

        List<String> labels = new ArrayList<>();
        for(int i=0; i < output.size(); ++i){
            labels.add(output.get(i)._1);
        }

        setPositiveClassLabel(labels.get(0));
    }
}
