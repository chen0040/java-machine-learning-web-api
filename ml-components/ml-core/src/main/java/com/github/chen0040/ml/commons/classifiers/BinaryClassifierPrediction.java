package com.github.chen0040.ml.commons.classifiers;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class BinaryClassifierPrediction {
    private BinaryClassifier classifier;
    private double score;

    public BinaryClassifierPrediction(BinaryClassifier classifier, double score){
        this.classifier = classifier;
        this.score = score;
    }

    public BinaryClassifier getClassifier() {
        return classifier;
    }

    public double getScore() {
        return score;
    }
}
