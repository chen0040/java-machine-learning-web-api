package com.github.chen0040.ml.spark.core.classifiers;


import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
public abstract class AbstractClassifier extends AbstractSparkMLModule implements Classifier {
    private List<String> classLabels;



    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        AbstractClassifier rhs2 = (AbstractClassifier)rhs;
        classLabels.clear();

        for(int i=0; i < rhs2.classLabels.size(); ++i){
            classLabels.add(rhs2.classLabels.get(i));
        }
    }

    public AbstractClassifier(List<String> classLabels)
    {
        this.classLabels = classLabels;
        setIsLabelRequiredInBatchUpdate(true);
    }

    public AbstractClassifier(){
        this.classLabels = new ArrayList<>();
        setIsLabelRequiredInBatchUpdate(true);
    }

    public List<String> getClassLabels(){
        return this.classLabels;
    }

    public void setClassLabels(List<String> classLabels){
        this.classLabels = classLabels;
    }

    public abstract String predict(SparkMLTuple tuple);

    protected void scan4ClassLabel(JavaRDD<SparkMLTuple> batch){

        JavaPairRDD<String, Integer> rdd = batch.mapToPair(new PairFunction<SparkMLTuple, String, Integer>() {
            public Tuple2<String, Integer> call(SparkMLTuple tuple) throws Exception {
                return new Tuple2<String, Integer>(tuple.getLabelOutput(), 1);
            }
        });

        JavaPairRDD<String, Integer> rdd2 = rdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = rdd2.collect();

        System.out.println(" output size: "+output.size());

        List<String> labels = new ArrayList<>();
        for(int i=0; i < output.size(); ++i){
            System.out.println("label: "+ output.get(i)._1);
            labels.add(output.get(i)._1);
        }

        setClassLabels(labels);

    }

}
