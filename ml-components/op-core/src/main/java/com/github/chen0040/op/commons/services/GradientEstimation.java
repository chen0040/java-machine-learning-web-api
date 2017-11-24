package com.github.chen0040.op.commons.services;

import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class GradientEstimation
{

    public static double small()
    {
        double one, two, z, tsmall;
        one = 1;
        two = 2;
        tsmall = one;
        do
        {
            tsmall = tsmall / two;
            z = fool(tsmall, one);
        } while (z > 1);
        return tsmall * two * two;
    }

    public static double fool(double x, double y)
    {
        return x * y + y;
    }

    //calculate gradient
    public static void calcGradient(double[] solution, double[] gradf, CostEvaluationMethod evaluate, double[] lowerBounds, double[] upperBounds, Object constraint)
    {
        double xi, delta;
        double udelta = 0.0;

        int dimension_count = solution.length;

        double rteps = Math.sqrt(small());
        for (int i = 0; i < dimension_count; i++)
        {
            xi = solution[i];

            double tmp = 1.0e0;
            if (1.0e0 > Math.abs(xi))
            {
                tmp = 1.0e0;
            }
            else
            {
                tmp = Math.abs(xi);
            }
            if (udelta > rteps * tmp)
            {
                delta = udelta;
            }
            else
            {
                delta = rteps * tmp;
            }
            if (xi < 0.0) delta = -delta;

            solution[i] = xi + delta;
            double f2 = evaluate.apply(solution, lowerBounds, upperBounds, constraint);
            solution[i] = xi;
            double f1 = evaluate.apply(solution, lowerBounds, upperBounds, constraint);
            if (Double.isInfinite(f1) && Double.isInfinite(f2))
            {
                gradf[i] = 0;
            }
            else
            {
                gradf[i] = (f2 - f1) / delta;
            }
        }
        return;
    }

}

