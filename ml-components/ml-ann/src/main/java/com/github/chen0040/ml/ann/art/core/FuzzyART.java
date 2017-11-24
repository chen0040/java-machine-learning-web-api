package com.github.chen0040.ml.ann.art.core;

/**
 * Created by memeanalytics on 21/8/15.
 * Link:
 * http://medusa.sdsu.edu/Robotics/Neuromuscular/Theses/Hongyu/chapter3.pdf
 */
public class FuzzyART extends ART1 {

    public FuzzyART(int inputCount, int initialNeuronCount) {
        super(inputCount, initialNeuronCount);
    }

    public FuzzyART(){
        super();
    }

    @Override
    public Object clone(){
        FuzzyART clone = new FuzzyART();
        clone.copy(this);

        return clone;
    }

    @Override
    protected double choice_function(double[] x, int j){
        double[] W_j = weights.get(j);
        double sum = 0;
        double sum2 = 0;
        for (int i = 0; i < x.length; ++i){
            sum += Math.abs(Math.min(x[i], W_j[i])); // norm1(fuzzy and)
            sum2 += Math.abs(W_j[i]); //  norm1
        }

        return sum / (alpha + sum2);
    }

    @Override
    protected double match_function(double[] x, int j){
        double[] W_j = weights.get(j);
        double sum = 0;
        double sum2 = 0;
        for (int i = 0; i < x.length; ++i){
            sum += Math.abs(Math.min(x[i], W_j[i])); // norm1(fuzzy and)
            sum2 += Math.abs(x[i]); //  norm1
        }

        return sum / sum2;
    }
}
