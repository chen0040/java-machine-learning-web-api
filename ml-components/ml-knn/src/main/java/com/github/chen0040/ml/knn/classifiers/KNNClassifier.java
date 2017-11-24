package com.github.chen0040.ml.knn.classifiers;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.AbstractClassifier;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class KNNClassifier extends AbstractClassifier {
    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private int neighborhoodSize = 5;
    private IntelliContext model;

    public IntelliContext getModel(){
        return model;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        KNNClassifier rhs2 = (KNNClassifier)rhs;
        neighborhoodSize = rhs2.neighborhoodSize;
        distanceMeasure = rhs2.distanceMeasure;
        model = rhs2.model == null ? null : rhs2.model.clone();
    }

    @Override
    public Object clone(){
        KNNClassifier clone = new KNNClassifier();
        clone.copy(this);
        return clone;
    }

    public KNNClassifier(List<String> classLabels){
        super(classLabels);
    }

    public KNNClassifier(){
        super();
    }

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public void setNeighborhoodSize(int neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
    }

    @Override
    public String predict(IntelliTuple tuple) {
        Map<IntelliTuple, Double> neighbors = DistanceMeasureService.getKNearestNeighbors(model, tuple, neighborhoodSize, distanceMeasure);
        Map<String, Integer> labelFrequencies = new HashMap<String, Integer>();
        for(IntelliTuple ti : neighbors.keySet()){
            String label = ti.getLabelOutput();
            if(labelFrequencies.containsKey(label)){
                labelFrequencies.put(label, labelFrequencies.get(label)+1);
            }else{
                labelFrequencies.put(label, 1);
            }
        }
        String selectedLabel = null;
        int maxFrequency = 0;
        for(String label : labelFrequencies.keySet()){
            int frequency = labelFrequencies.get(label);
            if(frequency > maxFrequency){
                maxFrequency = frequency;
                selectedLabel = label;
            }
        }

        return selectedLabel;
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch0) {
        this.setModelSource(batch0);
        this.model = batch0.clone();

        model = model.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        if(getClassLabels().isEmpty()){
            HashSet<String> set = new HashSet<>();
            for(int i = 0; i < model.tupleCount(); i++){
                set.add(model.tupleAtIndex(i).getLabelOutput());
            }
            List<String> labels = new ArrayList<>();
            for(String label : set){
                labels.add(label);
            }
            setClassLabels(labels);
        }

        for(int i = 0; i < model.tupleCount(); ++i){
            IntelliTuple tuple = model.tupleAtIndex(i);
            tuple.setPredictedLabelOutput(predict(tuple));
        }

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
