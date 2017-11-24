package com.github.chen0040.ml.glm.links;

/**
 * Created by memeanalytics on 14/8/15.
 */
/// <summary>
/// For the logistic link function:
/// Constraint interval : [0, 1]
///
/// The logistic link function maps constraint interval {b | b \in [0, 1]} to the real line {a | a \in R } by:
/// a = f(b) = log(b / (1-b))
///
/// The inverse link function maps the real line {a | a \in R} to constraint interval {b | b \in [0, 1] } by:
/// b = g(a) = 1 / (1 + exp(-a))
///
/// This is generally application to the following distribution (logistic regressions):
///   1. Binomial: count of # of "yes" occurrence out of N yes/no occurrences
///   2. Bernouli: count of single yes/no occurrence
///   3. Categorical: outcome of single K-way occurrence
///   4. Multinomial: cout of occurrences of different types (1 .. K) out of N total K-way occurrences
/// </summary>
public class LogitLinkFunction extends AbstractLinkFunction {

    @Override
    public Object clone(){
        return new LogitLinkFunction();
    }

    /// <summary>
    /// The logistic link function maps constraint interval {b | b \in [0, 1]} to the real line {a | a \in R } by:
    /// a = f(b) = log(b / (1-b))
    /// </summary>
    /// <param name="b">The constraint interval value</param>
    /// <returns>The mapped linear line value</returns>
    @Override
    public double GetLink(double b) {
        return Math.log(b / (1 - b));
    }

    /// <summary>
    /// The inverse link function maps the real line {a | a \in R} to constraint interval {b | b \in [0, 1] } by:
    /// b = g(a) = 1 / (1 + exp(-a))
    /// </summary>
    /// <param name="a">the real line value</param>
    /// <returns>the mapped constraint interval value</returns>
    @Override
    public double GetInvLink(double a) {
        return 1.0 / (1.0 + Math.exp(-a));
    }

    @Override
    public double GetInvLinkDerivative(double a) {
        //double g_a = GetInvLink(a);
        //return g_a * (1 - g_a);

        double t = Math.exp(-a);
        return t / ((1 + t) * (1 + t));
    }
}

