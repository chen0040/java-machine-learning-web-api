package com.github.chen0040.statistics.distributions.multivariate;

import Jama.Matrix;

import java.util.List;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface MultiVariateDistribution extends Cloneable {
    void copy(MultiVariateDistribution rhs);

    Matrix getMu();
    Matrix getCovariance();

    double getProbabilityDensity(double[] x);
    void sample(List<double[]> values);

    boolean isDegenerate();

    boolean inPredictionInterval(double[] x, double f);




}
