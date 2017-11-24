package com.github.chen0040.op.commons.services;

import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.costs.CostFunction;
import com.github.chen0040.op.commons.models.misc.LineSearchResult;
import com.github.chen0040.op.commons.models.costs.GradientEvaluationMethod;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class LineSearch {
    private final static double SIGMA = 1.0E-4;
    private final static double BETA = 0.5;
    private final static double ZERO = 1.0E-10;

    public static LineSearchResult search(double[] x_0, double fx_0, double[] direction, CostEvaluationMethod evaluate, GradientEvaluationMethod calc_gradient, double[] lower_bounds, double[] upper_bounds, Object constraints)
    {
        int dimension = x_0.length;

        double[] Vfx = new double[dimension];
        double[] x_out = new double[dimension];
        double alpha = 1.0;
        double fx_out = Double.MAX_VALUE;

        calc_gradient.apply(x_0, Vfx, lower_bounds, upper_bounds, constraints);

        double direction_length = 0;
        for (int d = 0; d < dimension; ++d) {
            direction_length += direction[d] * direction[d];
        }
        direction_length = Math.sqrt(direction_length);

        if (direction_length > 0)
        {
            for (int d = 0; d < dimension; ++d)
            {
                direction[d] /= direction_length;
            }
        }

        double p = 0.0;
        for (int d = 0; d < dimension; ++d)
        {
            p += (direction[d] * Vfx[d]);
        }

        if (Double.isNaN(p))
        {
            return new LineSearchResult(x_out, fx_out, alpha, false);
        }

        if (p >= 0.0) // not in the descending direction return false;
        {
            return new LineSearchResult(x_out, fx_out, alpha, false);
        }


        for (int k = 0; ; ++k)
        {
            for (int d = 0; d < dimension; ++d) {
                x_out[d] = x_0[d] + alpha * direction[d];
            }
            fx_out = evaluate.apply(x_out, lower_bounds, upper_bounds, constraints);

            if (fx_out < fx_0 + SIGMA * alpha * p)
            {
                return new LineSearchResult(x_out, fx_out, alpha, true);
            }
            else
            {
                if (k == 0)
                {
                    double enumerator = (p + fx_0 - fx_out);
                    if (enumerator == 0)
                    {
                        alpha = 0.5 * p / enumerator;
                    }
                    else
                    {
                        alpha = 0.5 * p;
                    }

                    //Console.WriteLine("alpha: {0}", alpha);
                }
                else
                {
                    alpha *= BETA;
                }
            }

            //Console.WriteLine("alpha: {0}", alpha);

            if (alpha < ZERO)
            {
                if (fx_out > fx_0)
                {
                    for (int d = 0; d < dimension; ++d)
                    {
                        x_out[d] = x_0[d];
                    }

                    fx_out = fx_0;
                    return new LineSearchResult(x_out, fx_out, alpha, true);
                }
                else
                {
                    return new LineSearchResult(x_out, fx_out, alpha, true);
                }
            }
        }
    }

    public static LineSearchResult search(double[] x_0, double fx_0, double[] direction, CostFunction f)
    {
        int dimension = x_0.length;

        double[] Vfx = new double[dimension];
        double[] x_out = new double[dimension];
        double alpha = 1.0;
        double fx_out = Double.MAX_VALUE;

        f.calcGradient(x_0, Vfx);

        double direction_length = 0;
        for (int d = 0; d < dimension; ++d)
        {
            direction_length += direction[d] * direction[d];
        }
        direction_length = Math.sqrt(direction_length);

        if (direction_length > 0)
        {
            for (int d = 0; d < dimension; ++d)
            {
                direction[d] /= direction_length;
            }
        }

        double p = 0.0;
        for (int d = 0; d < dimension; ++d)
        {
            p += (direction[d] * Vfx[d]);
        }

        //Console.WriteLine("p: {0}", p);


        if (Double.isNaN(p))
        {
            return new LineSearchResult(x_out, fx_out, alpha, false);
        }

        if (p >= 0.0) // not in the descending direction return false;
        {
            return new LineSearchResult(x_out, fx_out, alpha, false);
        }


        for (int k = 0; ; ++k)
        {
            for (int d = 0; d < dimension; ++d)
            {
                x_out[d] = x_0[d] + alpha * direction[d];
            }
            fx_out = f.evaluate(x_out);

            if (fx_out < fx_0 + SIGMA * alpha * p)
            {
                return new LineSearchResult(x_out, fx_out, alpha, true);
            }
            else
            {
                if (k == 0)
                {
                    double enumerator = (p + fx_0 - fx_out);
                    if (enumerator == 0)
                    {
                        alpha = 0.5 * p / enumerator;
                    }
                    else
                    {
                        alpha = 0.5 * p;
                    }

                    //Console.WriteLine("alpha: {0}", alpha);
                }
                else
                {
                    alpha *= BETA;
                }
            }

            //Console.WriteLine("alpha: {0}", alpha);

            if (alpha < ZERO)
            {
                if (fx_out > fx_0)
                {
                    for (int d = 0; d < dimension; ++d)
                    {
                        x_out[d] = x_0[d];
                    }

                    fx_out = fx_0;
                    return new LineSearchResult(x_out, fx_out, alpha, true);
                }
                else
                {
                    return new LineSearchResult(x_out, fx_out, alpha, true);
                }
            }
        }

    }
}
