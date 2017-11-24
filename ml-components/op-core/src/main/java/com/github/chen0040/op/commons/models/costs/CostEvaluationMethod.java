package com.github.chen0040.op.commons.models.costs;

/**
 * Created by memeanalytics on 12/8/15.
 */
public interface CostEvaluationMethod{

    double apply(double[] x, double[] lowerBounds, double[] upperBounds, Object constraint);
}
