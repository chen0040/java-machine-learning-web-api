package com.github.chen0040.ml.bayes.nbc;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.classifiers.AbstractClassifier;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.counts.CountRepository;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevel;
import com.github.chen0040.ml.dataprepare.discretize.kmeans.KMeansDiscretizer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class NBC extends AbstractClassifier {
    private final static Logger logger = Logger.getLogger(String.valueOf(NBC.class));
    private CountRepository model;
    private InputDataMode dataMode = InputDataMode.Categorical;


    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        NBC rhs2 = (NBC)rhs;
        model = rhs2.model == null ? null : (CountRepository)rhs2.model.clone();
        dataMode = rhs2.dataMode;
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
    public String predict(IntelliTuple tuple) {
        int n = tuple.tupleLength();

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

    public HashMap<String, Double> getScores(IntelliTuple tuple)
    {
        IntelliContext context = getModelSource();
        HashMap<String, Double> scores = new HashMap<String, Double>();
        for(String classLabel : getClassLabels()) {
            String classEventName = "ClassLabel="+classLabel;

            double score = 1;

            double pC = model.getProbability(classEventName);
            score *= pC;

            int n = tuple.tupleLength();
            for (int i = 0; i < n; ++i) {
                String value = getValue(tuple, i);
                String variableName = context.getAttributeName(i);
                String eventName = variableName + "=" + value;

                double pXiC = model.getConditionalProbability(classEventName, eventName);
                score *= pXiC;
            }

            scores.put(classLabel, score);
        }

        return scores;
    }

    private String getValue(IntelliTuple tuple, int j){
        String value;
        IntelliContext context = getModelSource();
        if(context.isCategorical(j)) {
            TupleAttributeLevel level = context.getAttributeValueAsLevel(tuple, j);
            value = level.getLevelName();
        }else{
            int discreteValue = inputDiscretizer.discretize(context.getAttributeValueAsDouble(tuple, j, 0), j);
            value = String.format("%d", discreteValue);
        }

        return value;
    }

    private void initializeClassLabels(IntelliContext batch){
        HashSet<String> classLabels = new HashSet<>();
        int m = batch.tupleCount();

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            if(isValidTrainingSample(tuple)) {
                classLabels.add(tuple.getLabelOutput());
            }
        }

        List<String> classLabelList = new ArrayList<>();
        for(String label : classLabels){
            classLabelList.add(label);
        }
        setClassLabels(classLabelList);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {

        this.setModelSource(batch);

        batch = batch.filter(new Function<IntelliTuple, Boolean>(){
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        if(dataMode == InputDataMode.Categorical) {
            inputDiscretizer.batchUpdate(batch);
        }

        model = new CountRepository();

        initializeClassLabels(batch);

        int m = batch.tupleCount();

        for(int i=0; i < m ; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);

            String classEventName = "ClassLabel=" + tuple.getLabelOutput();



            if(dataMode == InputDataMode.Categorical) {
                int n = tuple.tupleLength();
                for (int j = 0; j < n; ++j) {
                    String variableName = batch.getAttributeName(j);
                    String value = getValue(tuple, j);
                    String eventName = variableName + "=" + value;
                    model.addSupportCount(classEventName, eventName);
                    model.addSupportCount(classEventName);
                    model.addSupportCount();
                }
            } else if(dataMode == InputDataMode.HitCount){
                double[] x = batch.toNumericArray(tuple);
                int n = x.length;
                for(int j=0; j < n; ++j){
                    String eventName = batch.getAttributeValueAsLevel(tuple, j).toString();
                    model.addSupportCount(x[j], classEventName, eventName);
                    model.addSupportCount(x[j], classEventName);
                    model.addSupportCount(x[j]);
                }

            }
        }

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        throw new NotImplementedException();
    }

    public enum InputDataMode{
        HitCount, // in the case where attributes in the input data represent frequency of attribute appearing in the input data
        Categorical // in the case where attributes in the iput data represent on / off state of an event appearing in the input data
    }
}
