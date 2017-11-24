package com.github.chen0040.ml.gaussianmixture;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.statistics.distributions.univariate.NormalDistribution;

import java.util.*;

public class NormalOutliers extends AbstractAnomalyDetecter {

    private static final double DEFAULT_VALUE = 0;
    protected HashMap<Integer, NormalDistribution> model;

    public static final String THRESHOLD = "threshold";
    public static final String AUTO_THRESHOLDING = "automatic thresholding";
    public static final String AUTO_THRESHOLDING_RATIO = "anomaly ratio in automatic thresholding";

    public NormalOutliers(){
        super();
        model = new HashMap<Integer, NormalDistribution>();
        setThreshold(0.02);
        setAttribute(AUTO_THRESHOLDING, 1);
        setAttribute(AUTO_THRESHOLDING_RATIO, 0.05);
    }

    protected boolean isAutoThresholding(){
        return (int)getAttribute(AUTO_THRESHOLDING) > 0;
    }

    protected int autoThresholdingCaps(int m){
        return Math.max(1, (int) (getAttribute(AUTO_THRESHOLDING_RATIO) * m));
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        NormalOutliers rhs2 = (NormalOutliers)rhs;
        model.clear();
        for(Integer i : rhs2.model.keySet()){
            NormalDistribution d = rhs2.model.get(i);
            model.put(i, d == null ? null : (NormalDistribution)d.clone());
        }
    }

    @Override
    public Object clone(){
        NormalOutliers clone = new NormalOutliers();
        clone.copy(this);
        return clone;
    }

    public HashMap<Integer, NormalDistribution> getModel() {
        return model;
    }

    public void setModel(HashMap<Integer, NormalDistribution> model) {
        this.model = model;
    }

    private double threshold() {
        return getAttribute(THRESHOLD);
    }

    public void setThreshold(double threshold) {
        this.setAttribute(THRESHOLD, threshold);
    }

    public void scratch(){
        model = new HashMap<Integer, NormalDistribution>();
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple){
        double p = calculateProbability(tuple);
        return p < threshold();
    }

    // translate the tuple into a probability value indicating the chance of the tuple being in the NORMAL class
    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        return calculateProbability(tuple);
    }

    public double calculateProbability(IntelliTuple tuple){
        double product = 1;
        double[] x = getModelSource().toNumericArray(tuple);
        for(int i=0; i < x.length; ++i){
            product *= calculateFeatureProbability(i, x[i]);
        }
        return product;
    }

    protected double calculateFeatureProbability(int index, double value){
        NormalDistribution distribution = model.get(index);
        return distribution.getProbabilityDensity(value);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch){
        int size = batch.tupleCount();
        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        for(int k=0; k < dimension; ++k) {
            List<Double> values = new ArrayList<Double>();

            for (int i = 0; i < size; ++i) {
                IntelliTuple tuple = batch.tupleAtIndex(i);
                double[] x = batch.toNumericArray(tuple);
                if (tuple != null) {
                    values.add(x[k]);
                }
            }
            NormalDistribution distribution = model.get(k);
            if(distribution==null){
                distribution=new NormalDistribution();
                model.put(k, distribution);
            }
            distribution.sample(values);
        }

        if(isAutoThresholding()){
            adjustThreshold(batch);
        }

        return new BatchUpdateResult();
    }

    protected void adjustThreshold(IntelliContext batch){
        int m = batch.tupleCount();

        List<Integer> orders = new ArrayList<Integer>();
        List<Double> probs = new ArrayList<Double>();

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double prob = evaluate(tuple, batch);
            probs.add(prob);
            orders.add(i);
        }

        final List<Double> probs2 = probs;
        // sort ascendingly by probability values
        Collections.sort(orders, new Comparator<Integer>() {
            public int compare(Integer h1, Integer h2) {
                double prob1 = probs2.get(h1);
                double prob2 = probs2.get(h2);
                return Double.compare(prob1, prob2);
            }
        });

        int selected_index = autoThresholdingCaps(orders.size());
        if(selected_index >= orders.size()){
            setThreshold(probs.get(orders.get(orders.size()-1)));
        }
        else{
            setThreshold(probs.get(orders.get(selected_index)));
        }

    }

    public double tune(IntelliContext trainingData, IntelliContext crossValudation, double confidence_level){
        batchUpdate(trainingData);
        IntelliContext crossValidation = trainingData.clone();
        int n = crossValidation.tupleCount();
        double error_rate = 1 - confidence_level;

        List<Integer> orders = new ArrayList<Integer>();
        final double[] p = new double[n];
        for(int i=0; i < n; ++i){
            p[i] = evaluate(crossValidation.tupleAtIndex(i), crossValidation);
            orders.add(i);
        }

        // sort ascending based on the values of p
        Collections.sort(orders, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                double p1 = p[o1];
                double p2 = p[o2];

                return Double.compare(p1, p2);
            }
        });

        int anomaly_count = Math.min((int) Math.ceil(error_rate * n), n);

        int anomaly_separator_index = orders.get(anomaly_count - 1);
        double best_threshold = p[anomaly_separator_index];

        this.setThreshold(best_threshold);

        return best_threshold;

    }


}
