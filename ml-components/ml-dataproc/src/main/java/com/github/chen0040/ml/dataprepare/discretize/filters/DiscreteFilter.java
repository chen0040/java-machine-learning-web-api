package com.github.chen0040.ml.dataprepare.discretize.filters;

/**
 * Created by memeanalytics on 18/8/15.
 */
public interface DiscreteFilter {
    void addValue(double value);
    void build();
    int discretize(double value);
}
