package com.github.chen0040.statistics.distributions.univariate;

import java.util.List;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface Distribution extends Cloneable {

    void copy(Distribution rhs);

    double getMu();
    double getSigma();

    double getProbabilityDensity(double x);
    void sample(List<Double> values);
}
