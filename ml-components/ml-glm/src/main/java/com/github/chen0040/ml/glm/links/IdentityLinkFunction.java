package com.github.chen0040.ml.glm.links;

/**
 * Created by memeanalytics on 14/8/15.
 */
/// <summary>
/// For the linear link function:
/// The constraint interval is a real line as well
///
/// The linear link function maps constraint interval { b | b \in R } to real line {a | a \in R} by:
/// a = f(b) = b
///
/// The inverse link function maps real line {a | a \in R} to constraint interval { b | b \in R } by:
/// b = g(a) = a
///
/// Generally applicable to the following distribution:
///   1. Normal: which is the linear-response shrinkedData (linear regressions)
/// </summary>
public class IdentityLinkFunction extends AbstractLinkFunction {

    @Override
    public Object clone(){
        IdentityLinkFunction clone = new IdentityLinkFunction();
        return clone;
    }

    /// <summary>
    /// The linear link function maps constraint interval { b | b \in R } to real line {a | a \in R} by:
    /// a = f(b) = b
    /// </summary>
    /// <param name="b">The constraint interval value</param>
    /// <returns>The mapped linear line value</returns>
    @Override
    public double GetLink(double b) {
        return b;
    }

    /// <summary>
    /// The inverse link function maps real line {a | a \in R} to constraint interval { b | b \in R } by:
    /// b = g(a) = a
    /// </summary>
    /// <param name="a">The linear line value</param>
    /// <returns>The mapped constraint interval value</returns>
    @Override
    public double GetInvLink(double a) {
        return a;
    }

    @Override
    public double GetInvLinkDerivative(double real_line_value) {
        return 1;
    }
}

