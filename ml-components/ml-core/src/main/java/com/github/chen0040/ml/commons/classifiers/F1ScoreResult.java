package com.github.chen0040.ml.commons.classifiers;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class F1ScoreResult {
    private double F1Score;
    private double precision;
    private double recall;

    public double getF1Score() {
        return F1Score;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public F1ScoreResult(double f1score, double precision, double recall){
        this.F1Score = f1score;
        this.recall = recall;
        this.precision = precision;
    }

    @Override
    public String toString(){
        return String.format("F1: %.3f\tprecision: %.3f\trecall: %.3f", F1Score, precision, recall);
    }
}
