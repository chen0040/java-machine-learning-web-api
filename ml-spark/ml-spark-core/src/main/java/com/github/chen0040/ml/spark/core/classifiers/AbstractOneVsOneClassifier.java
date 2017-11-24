package com.github.chen0040.ml.spark.core.classifiers;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.PredictionResult;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public abstract class AbstractOneVsOneClassifier extends AbstractClassifier {
    protected List<BinaryClassifier[]> classifiers;
    private double alpha = 0.1;

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        AbstractOneVsOneClassifier rhs2 = (AbstractOneVsOneClassifier)rhs;
        alpha = rhs2.alpha;
        classifiers.clear();
        for(int i=0; i < rhs2.classifiers.size(); ++i){
            BinaryClassifier[] bclassifiers_rhs = rhs2.classifiers.get(i);
            BinaryClassifier[] bclassifiers = new BinaryClassifier[bclassifiers_rhs.length];
            for(int j=0; j < bclassifiers_rhs.length; ++j){
                AbstractBinaryClassifier abc = (AbstractBinaryClassifier)bclassifiers_rhs[j];
                bclassifiers[j] = abc==null ? null : (BinaryClassifier)abc.clone();
            }
            classifiers.add(bclassifiers);
        }

    }

    public AbstractOneVsOneClassifier(List<String> classLabels){
        super(classLabels);
        classifiers = new ArrayList<BinaryClassifier[]>();
    }

    public AbstractOneVsOneClassifier(){
        super();
        classifiers = new ArrayList<BinaryClassifier[]>();
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    protected void createClassifiers(JavaRDD<SparkMLTuple> batch){
        classifiers = new ArrayList<BinaryClassifier[]>();
        List<String> labels = getClassLabels();
        if(labels.size()==0){
            scan4ClassLabel(batch);
            labels = getClassLabels();
        }
        for(int i=0; i < labels.size(); ++i){
            for(int j=i+1; j < labels.size(); ++j) {
                BinaryClassifier[] pair = new BinaryClassifier[2];
                pair[0] = createClassifier(labels.get(i));
                pair[1] = createClassifier(labels.get(j));
                classifiers.add(pair);
            }
        }
    }

    protected abstract BinaryClassifier createClassifier(String classLabel);
    protected abstract double getClassifierScore(SparkMLTuple tuple, BinaryClassifier classifier);



    protected boolean isValidTrainingSample(SparkMLTuple tuple){
        return tuple.getLabelOutput() != null;
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {

        batch = batch.filter(new Function<SparkMLTuple, Boolean>() {
             public Boolean call(SparkMLTuple tuple) throws Exception {
                 return isValidTrainingSample(tuple);
             }
         });

        createClassifiers(batch);

        int C = classifiers.size();
        double weight = 1.0 / C;
        double[] weights = new double[C];
        for(int i=0; i < C; ++i){
            weights[i] = weight;
        }

        JavaRDD<SparkMLTuple>[] batches = batch.randomSplit(weights);

        Exception error = null;
        for(int i=0; i < classifiers.size(); ++i){
            BinaryClassifier[] pair = classifiers.get(i);
            BinaryClassifier classifier1 = pair[0];
            BinaryClassifier classifier2 = pair[1];

            BatchUpdateResult classifierResult1 = classifier1.batchUpdate(batches[i]);
            BatchUpdateResult classifierResult2 = classifier2.batchUpdate(batches[i]);

            if(!classifierResult1.success() && error == null){
                error = classifierResult1.getError();
            }

            if(!classifierResult2.success() && error == null){
                error = classifierResult2.getError();
            }
        }

        return new BatchUpdateResult(error);
    }

    @Override
    public String predict(SparkMLTuple tuple) {

        String predicatedClassLabel = null;
        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for(int i=0; i < classifiers.size(); ++i){
            BinaryClassifier[] pair = classifiers.get(i);
            BinaryClassifier classifier1 = pair[0];
            BinaryClassifier classifier2 = pair[1];

            double score1 = getClassifierScore(tuple, classifier1);
            double score2 = getClassifierScore(tuple, classifier2);

            String winningLabel;
            if(score1 > score2) {
                winningLabel = classifier1.getPositiveClassLabel();
            }
            else {
                winningLabel = classifier2.getPositiveClassLabel();
            }
            if(scores.containsKey(winningLabel)){
                scores.put(winningLabel, scores.get(winningLabel) + 1);
            }else {
                scores.put(winningLabel, 1);
            }
        }

        int maxScore = 0;
        for(String label : scores.keySet()){
            int score = scores.get(label);
            if(score > maxScore){
                maxScore= score;
                predicatedClassLabel = label;
            }
        }

        return predicatedClassLabel;
    }

    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        throw new NotImplementedException();
    }
}
