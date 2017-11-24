package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;

import java.util.HashMap;

/**
 * Created by memeanalytics on 15/8/15.
 */
public abstract class AbstractBinaryClassifier extends AbstractMLModule implements  BinaryClassifier {
    private String positiveClassLabel;
    private String negativeClassLabel;

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

    public abstract boolean isInClass(IntelliTuple tuple, IntelliContext context);

    protected void scan4ClassLabel(IntelliContext batch){
        int m = batch.tupleCount();
        HashMap<String, Integer> labelCounts = new HashMap<String, Integer>();
        for (int i = 0; i < m; ++i) {
            String label = batch.tupleAtIndex(i).getLabelOutput();
            if (label != null) {
                if (labelCounts.containsKey(label)) {
                    labelCounts.put(label, labelCounts.get(label) + 1);
                } else {
                    labelCounts.put(label, 1);
                }
            }
        }

        int maxCount = 0;
        String defaultLabel = null;
        for(String label : labelCounts.keySet()){
            int count = labelCounts.get(label);
            if(count > maxCount){
                defaultLabel = label;
                maxCount = count;
            }
        }

        setPositiveClassLabel(defaultLabel);

        if(labelCounts.size()==2){
            for(String label : labelCounts.keySet()){
                if(!label.equals(getPositiveClassLabel())){
                    setNegativeClassLabel(label);
                    break;
                }
            }
        }
    }
}
