package com.github.chen0040.op.commons.models.costs;

/**
 * Created by memeanalytics on 12/8/15.
 */
public interface GradientEvaluationMethod {
    void apply(double[] x, double[] Vf, double[] lowerBounds, double[] upperBounds, Object constraint);
}
