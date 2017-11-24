package com.github.chen0040.ml.ann.mlp.regression;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.regressions.AbstractRegression;
import com.github.chen0040.ml.commons.regressions.PredictionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 21/8/15.
 */
public class MLPRegression extends AbstractRegression {
    private MLPWithNumericOutput mlp;
    private int epoches;
    private List<Integer> hiddenLayers;
    private double learningRate;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        MLPRegression rhs2 = (MLPRegression)rhs;
        mlp = rhs2.mlp == null ? null : (MLPWithNumericOutput)rhs2.mlp.clone();
        epoches = rhs2.epoches;
        hiddenLayers.clear();
        for(int i=0; i < rhs2.hiddenLayers.size(); ++i){
            hiddenLayers.add(rhs2.hiddenLayers.get(i));
        }
        learningRate = rhs2.learningRate;
    }

    @Override
    public Object clone(){
        MLPRegression clone = new MLPRegression();
        clone.copy(this);
        return clone;
    }

    public MLPRegression(){
        epoches = 1000;

        learningRate = 0.2;
        hiddenLayers = new ArrayList<Integer>();
        hiddenLayers.add(6);
    }

    public List<Integer> getHiddenLayers() {
        return hiddenLayers;
    }

    public void setHiddenLayers(int... hiddenLayers) {
        this.hiddenLayers = new ArrayList<Integer>();
        for(int hiddenLayerNeuronCount : hiddenLayers){
            this.hiddenLayers.add(hiddenLayerNeuronCount);
        }
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getEpoches() {
        return epoches;
    }

    public void setEpoches(int epoches) {
        this.epoches = epoches;
    }

    @Override
    public PredictionResult predict(IntelliTuple tuple) {
        double[] target = mlp.predict(getModelSource(), tuple);
        PredictionResult result = new PredictionResult();
        result.predictedOutput = target[0];
        return result;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);

        mlp = new MLPWithNumericOutput();
        mlp.setNormalizeOutputs(true);

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        mlp.setLearningRate(learningRate);
        mlp.createInputLayer(dimension);
        for (int hiddenLayerNeuronCount : hiddenLayers){
            mlp.addHiddenLayer(hiddenLayerNeuronCount);
        }
        mlp.createOutputLayer(1);

        mlp.train(batch, epoches);

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] target = mlp.predict(context, tuple);
        return target[0];
    }
}
