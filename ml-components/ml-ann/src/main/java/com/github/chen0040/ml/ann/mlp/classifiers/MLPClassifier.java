package com.github.chen0040.ml.ann.mlp.classifiers;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.AbstractClassifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class MLPClassifier extends AbstractClassifier {
    private MLPWithLabelOutput mlp;

    private final Logger logger = Logger.getLogger(String.valueOf(MLPClassifier.class));


    public static final String HIDDEN_LAYER1 = "hiddenLayer1";
    public static final String HIDDEN_LAYER2 = "hiddenLayer2";
    public static final String HIDDEN_LAYER3 = "hiddenLayer3";
    public static final String HIDDEN_LAYER4 = "hiddenLayer4";
    public static final String HIDDEN_LAYER5 = "hiddenLayer5";
    public static final String HIDDEN_LAYER6 = "hiddenLayer6";
    public static final String HIDDEN_LAYER7 = "hiddenLayer7";

    public static final String EPOCHES = "epoches";
    public static final String LEARNING_RATE = "learningRate";

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        MLPClassifier rhs2 = (MLPClassifier)rhs;
        mlp = rhs2.mlp == null ? null : (MLPWithLabelOutput)rhs2.mlp.clone();
        if(mlp != null){
            mlp.classLabelsModel = new Supplier<List<String>>() {
                public List<String> get() {
                    return getClassLabels();
                }
            };
        }
    }

    private double learningRate(){
        return getAttribute(LEARNING_RATE);
    }

    private void _learningRate(double value){
        setAttribute(LEARNING_RATE, value);
    }

    private int epoches(){
        return (int)getAttribute(EPOCHES);
    }

    private void _epoches(int value){
        setAttribute(EPOCHES, (double)value);
    }

    @Override
    public Object clone(){
        MLPClassifier clone = new MLPClassifier();
        clone.copy(this);

        return clone;
    }

    public MLPClassifier(){
        _epoches(1000);
        _learningRate(0.2);
        setAttribute(HIDDEN_LAYER1, 6);
        setAttribute(HIDDEN_LAYER2, 0);
        setAttribute(HIDDEN_LAYER3, 0);
        setAttribute(HIDDEN_LAYER4, 0);
        setAttribute(HIDDEN_LAYER5, 0);
        setAttribute(HIDDEN_LAYER6, 0);
        setAttribute(HIDDEN_LAYER7, 0);
    }

    public List<Integer> getHiddenLayers() {
        return parseHiddenLayers();
    }

    private String hiddenLayerName(int i){
        String hiddenLayerName = HIDDEN_LAYER7;
        switch(i){
            case 0:
                hiddenLayerName = HIDDEN_LAYER1;
                break;
            case 1:
                hiddenLayerName = HIDDEN_LAYER2;
                break;
            case 2:
                hiddenLayerName = HIDDEN_LAYER3;
                break;
            case 3:
                hiddenLayerName = HIDDEN_LAYER4;
                break;
            case 4:
                hiddenLayerName = HIDDEN_LAYER5;
                break;
            case 5:
                hiddenLayerName = HIDDEN_LAYER6;
                break;
            case 6:
                hiddenLayerName = HIDDEN_LAYER7;
                break;
        }
        return hiddenLayerName;
    }

    public void setHiddenLayers(int... hiddenLayers) {
        for(int i = 0; i < hiddenLayers.length; ++i){
            setAttribute(hiddenLayerName(i), hiddenLayers[i]);
        }
    }

    @Override
    public String predict(IntelliTuple tuple) {
        double[] target = mlp.predict(getModelSource(), tuple);

        int selected_index = -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        for(int i=0; i < target.length; ++i){
            double value = target[i];
            if(value > maxValue){
                maxValue = value;
                selected_index = i;
            }
        }

        if(selected_index==-1){
            logger.log(Level.SEVERE, "predict failed due to label not found");
        }

        return getClassLabels().get(selected_index);
    }

    private void scan4ClassLabels(IntelliContext batch){
        int m = batch.tupleCount();
        HashSet<String> set = new HashSet<>();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            if(tuple.hasLabelOutput()) {
                set.add(tuple.getLabelOutput());
            }
        }

        List<String> labels = new ArrayList<>();
        for(String label : set){
            labels.add(label);
        }

        setClassLabels(labels);
    }

    private List<Integer> parseHiddenLayers(){
        List<Integer> hiddenLayers = new ArrayList<Integer>();
        for(int i=0; i < 7; ++i){
            int neuronCount = (int)getAttribute(hiddenLayerName(i));
            if(neuronCount > 0){
                hiddenLayers.add(neuronCount);
            }
        }
        return hiddenLayers;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {

        this.setModelSource(batch);

        if (getClassLabels().isEmpty()) {
            scan4ClassLabels(batch);
        }

        mlp = new MLPWithLabelOutput();
        mlp.classLabelsModel = () -> getClassLabels();

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        List<Integer> hiddenLayers = parseHiddenLayers();

        mlp.setLearningRate(learningRate());
        mlp.createInputLayer(dimension);
        for (int hiddenLayerNeuronCount : hiddenLayers){
            mlp.addHiddenLayer(hiddenLayerNeuronCount);
        }
        mlp.createOutputLayer(getClassLabels().size());

        mlp.train(batch, epoches());

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] target = mlp.predict(context, tuple);

        int selected_index = -1;
        double maxValue = Double.MIN_VALUE;
        for(int i=0; i < target.length; ++i){
            double value = target[i];
            if(value > maxValue){
                maxValue = value;
                selected_index = i;
            }
        }

        return selected_index;
    }
}
