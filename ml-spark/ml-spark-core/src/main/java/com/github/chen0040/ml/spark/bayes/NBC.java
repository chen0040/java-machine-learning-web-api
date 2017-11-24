package com.github.chen0040.ml.spark.bayes;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.CountRepository;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.discrete.KMeansDiscretizer;
import com.github.chen0040.ml.spark.core.classifiers.AbstractClassifier;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import com.github.chen0040.ml.spark.core.discrete.AbstractAttributeValueDiscretizer;
import com.github.chen0040.ml.spark.core.discrete.AttributeValueDiscretizer;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class NBC extends AbstractClassifier {
    private final static Logger logger = Logger.getLogger(String.valueOf(NBC.class));
    private CountRepository model;
    private InputDataMode dataMode = InputDataMode.Categorical;
    private AttributeValueDiscretizer inputDiscretizer;

    protected boolean isValidTrainingSample(SparkMLTuple tuple){
        return tuple.getLabelOutput() != null;
    }

    public void makeColumnDiscrete(int columnIndex){
        inputDiscretizer.makeColumnDiscrete(columnIndex);
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        NBC rhs2 = (NBC)rhs;
        model = rhs2.model == null ? null : (CountRepository)rhs2.model.clone();
        dataMode = rhs2.dataMode;
        inputDiscretizer =  (AttributeValueDiscretizer)((AbstractAttributeValueDiscretizer)rhs2.inputDiscretizer).clone();
    }

    @Override
    public Object clone(){
        NBC clone = new NBC();
        clone.copy(this);

        return clone;
    }

    public NBC(){
        model = new CountRepository();
        inputDiscretizer = new KMeansDiscretizer();
    }

    public CountRepository getModel(){
        return model;
    }

    public InputDataMode getDataMode() {
        return dataMode;
    }

    public void setDataMode(InputDataMode dataMode) {
        this.dataMode = dataMode;
    }

    public AttributeValueDiscretizer getInputDiscretizer() {
        return inputDiscretizer;
    }

    public void setInputDiscretizer(AttributeValueDiscretizer inputDiscretizer) {
        this.inputDiscretizer = inputDiscretizer;
    }

    @Override
    public String predict(SparkMLTuple tuple) {
        int n = tuple.size();

        HashMap<String, Double> scores = getScores(tuple);

        double maxScore = 0;
        String bestLabel = null;
        for(String classLabel : scores.keySet()){
            double score = scores.get(classLabel);
            if(maxScore < score){
                maxScore = score;
                bestLabel = classLabel;
            }
        }

        tuple.setPredictedLabelOutput(bestLabel);

        return bestLabel;

    }

    public HashMap<String, Double> getScores(SparkMLTuple tuple)
    {
        HashMap<String, Double> scores = new HashMap<String, Double>();
        for(String classLabel : getClassLabels()) {
            String classEventName = "ClassLabel="+classLabel;

            double score = 1;

            double pC = model.getProbability(classEventName);
            score *= pC;

            int n = tuple.getDimension();
            for (int i = 0; i < n; ++i) {
                String value = getValue(tuple, i);

                String eventName = "Attributes[" + i + "] = " + value;

                double pXiC = model.getConditionalProbability(classEventName, eventName);
                score *= pXiC;
            }

            scores.put(classLabel, score);
        }

        return scores;
    }

    private String getValue(SparkMLTuple tuple, int j){
        String value;
        if(!inputDiscretizer.coverColumn(j)) {
            value = "" + tuple.getOrDefault(j, 0.0);
        }else{
            int discreteValue = inputDiscretizer.discretize(tuple.getOrDefault(j, 0.0), j);
            value = String.format("%d", discreteValue);
        }

        return value;
    }


    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {

        batch = batch.filter(new org.apache.spark.api.java.function.Function<SparkMLTuple, Boolean>() {
            public Boolean call(SparkMLTuple tuple) throws Exception {
                return isValidTrainingSample(tuple);
            }
        });

        scan4ClassLabel(batch);

        if(dataMode == InputDataMode.Categorical) {
            inputDiscretizer.batchUpdate(batch);
        }

        model = new CountRepository();

        int n = batch.first().getDimension();

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

        Map<String, Integer> class_output = rdd2.collectAsMap();

        for(String classLabel : class_output.keySet()){
            String classEventName = "ClassLabel=" + classLabel;
            int count = class_output.get(classLabel);
            model.addSupportCount(count, classEventName);
            model.addSupportCount(count);
        }

        for(int i=0; i < n; ++i){
            final int I = i;

            JavaPairRDD<Tuple2<String, String>, Integer> rdd3 = batch.mapToPair(new PairFunction<SparkMLTuple, Tuple2<String, String>, Integer>() {
                public Tuple2<Tuple2<String, String>, Integer> call(SparkMLTuple tuple) throws Exception {
                    String label = tuple.getLabelOutput();

                    double value = tuple.getOrDefault(I, 0.0);

                    String eventName = "Attributes[" + I + "] = " + value;

                    return new Tuple2<Tuple2<String, String>, Integer>(new Tuple2<String, String>(label, eventName), 1);
                }
            });

            JavaPairRDD<Tuple2<String, String>, Integer> rdd4 = rdd3.reduceByKey(new Function2<Integer, Integer, Integer>() {
                public Integer call(Integer i1, Integer i2) throws Exception {
                    return i1 + i2;
                }
            });

            List<Tuple2<Tuple2<String, String>, Integer>> event_output = rdd4.collect();
            for(int j=0; j < event_output.size(); ++j){
                Tuple2<Tuple2<String, String>, Integer> event_with_outcome = event_output.get(j);
                String classLabel = event_with_outcome._1()._1();
                String eventName = event_with_outcome._1()._2();
                int count = event_with_outcome._2();
                String classEventName = "ClassLabel=" + classLabel;
                model.addSupportCount(count, classEventName, eventName);
            }
        }



        return new BatchUpdateResult();
    }



    public enum InputDataMode{
        HitCount, // in the case where attributes in the input data represent frequency of attribute appearing in the input data
        Categorical // in the case where attributes in the iput data represent on / off state of an event appearing in the input data
    }
}
