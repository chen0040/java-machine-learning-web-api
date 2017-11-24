package com.github.chen0040.op.commons.models.solutions;

import java.util.Random;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class NumericSolutionFactory {
    private static Random r = new Random();
    public static double[] create(double[] lowerBounds, double[] upperBounds)
    {
        int dimensionCount = lowerBounds.length;
        double[] x_0 = new double[dimensionCount];

        for (int i = 0; i < dimensionCount; ++i)
        {
            x_0[i] = lowerBounds[i] + r.nextDouble() * (upperBounds[i] - lowerBounds[i]);
        }

        return x_0;
    }

    public static double sum(double[] x){
        double sum = 0;
        for(int i=0; i < x.length; ++i){
            sum+=x[i];
        }
        return sum;
    }

    public static double[] mutate(double[] x, double bounds){
        double sigma = Math.max(bounds, sum(x) / x.length);
        double[] prime = new double[x.length];
        for(int i=0; i < x.length; ++i){
            prime[i] = x[i] + bounds * (r.nextDouble() * 2 - 1);
        }
        return prime;
    }
}
