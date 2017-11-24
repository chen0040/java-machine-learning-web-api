package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public abstract class AbstractOneVsRestClassifier extends AbstractClassifier {
    private Map<String, BinaryClassifier> classifiers;

    @Override
    public void copy(MLModule rhs){
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

    protected void createClassifiers(IntelliContext batch){
        classifiers = new HashMap<String, BinaryClassifier>();
        if(getClassLabels().size()==0) {
            scan4ClassLabel(batch);
        }

        for(String label : getClassLabels()){
            classifiers.put(label, createClassifier(label));
        }
    }

    protected abstract BinaryClassifier createClassifier(String classLabel);
    protected abstract double getClassifierScore(IntelliTuple tuple, String classLabel);

    public BinaryClassifier getClassifier(String classLabel){
        return classifiers.get(classLabel);
    }

    @Override
    public String predict(IntelliTuple tuple) {
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

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
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
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
