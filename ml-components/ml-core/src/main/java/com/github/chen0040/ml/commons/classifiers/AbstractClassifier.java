package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliTuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
public abstract class AbstractClassifier extends AbstractMLModule implements Classifier {
    private List<String> classLabels;

    public double computePredictionAccuracy(IntelliContext batch){
        int m = batch.tupleCount();
        String label;
        int correctnessCount = 0;
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            label = tuple.getLabelOutput();
            if(label == null) continue;
            correctnessCount += (label.equals(tuple.getPredictedLabelOutput()) ? 1 : 0);
        }
        if(m == 0) return 0;
        return (double)correctnessCount / m;
    }

    @Override
    public void copy(MLModule rhs){
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

    public abstract String predict(IntelliTuple tuple);

    protected void scan4ClassLabel(IntelliContext batch){
        int m = batch.tupleCount();
        HashSet<String> labelSet = new HashSet<>();
        for (int i = 0; i < m; ++i) {
            String label = batch.tupleAtIndex(i).getLabelOutput();
            if (label != null) {
                labelSet.add(label);
            }
        }

        List<String> labels = new ArrayList<>();
        for(String label : labelSet){
            labels.add(label);
        }

        setClassLabels(labels);

    }

}
