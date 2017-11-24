package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.PredictionResult;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public abstract class AbstractOneVsRestClassifier extends AbstractClassifier {
    private Map<String, BinaryClassifier> classifiers;

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        classifiers.clear();

        AbstractOneVsRestClassifier rhs2 = (AbstractOneVsRestClassifier)rhs;
        for(String key : rhs2.classifiers.keySet()){
            AbstractBinaryClassifier abc = (AbstractBinaryClassifier) rhs2.classifiers.get(key);
            classifiers.put(key, abc==null ? null : (BinaryClassifier)abc.clone());
        }
    }

    public AbstractOneVsRestClassifier(List<String> classLabels){
        super(classLabels);
        classifiers = new HashMap<String, BinaryClassifier>();
    }

    public AbstractOneVsRestClassifier(){
        super();
        classifiers = new HashMap<String, BinaryClassifier>();
    }

    protected void createClassifiers(JavaRDD<SparkMLTuple> batch){
        classifiers = new HashMap<String, BinaryClassifier>();
        if(getClassLabels().size()==0) {
            scan4ClassLabel(batch);
        }

        for(String label : getClassLabels()){
            classifiers.put(label, createClassifier(label));
        }
    }

    protected abstract BinaryClassifier createClassifier(String classLabel);
    protected abstract double getClassifierScore(SparkMLTuple tuple, String classLabel);

    public BinaryClassifier getClassifier(String classLabel){
        return classifiers.get(classLabel);
    }

    @Override
    public String predict(SparkMLTuple tuple) {
        double maxScore = Double.MIN_VALUE;

        String predicatedClassLabel = null;
        for(String classLabel : getClassLabels()){
            double score = getClassifierScore(tuple, classLabel);

            if(score > maxScore){
                maxScore = score;
                predicatedClassLabel = classLabel;
            }
        }

        return predicatedClassLabel;
    }

    protected boolean isValidTrainingSample(SparkMLTuple tuple){
        return tuple.getLabelOutput() != null;
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {

        batch = batch.filter(new org.apache.spark.api.java.function.Function<SparkMLTuple, Boolean>() {
             public Boolean call(SparkMLTuple tuple) throws Exception {
                 return isValidTrainingSample(tuple);
             }
         });

        createClassifiers(batch);

        Exception error = null;
        for(String classLabel : getClassLabels()){
            BinaryClassifier classifier = getClassifier(classLabel);
            BatchUpdateResult classifierResult = classifier.batchUpdate(batch);

            if(!classifierResult.success() && error == null){
                error = classifierResult.getError();
            }
        }

        return new BatchUpdateResult(error);
    }



    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        throw new NotImplementedException();
    }
}
