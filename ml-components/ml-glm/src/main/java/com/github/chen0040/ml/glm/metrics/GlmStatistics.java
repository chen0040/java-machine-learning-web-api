package com.github.chen0040.ml.glm.metrics;

import Jama.Matrix;
import com.github.chen0040.statistics.metrics.StdDev;
import com.github.chen0040.statistics.metrics.Mean;

/**
 * Created by memeanalytics on 14/8/15.
 */
public class GlmStatistics implements Cloneable {
    protected double[][] VcovMatrix;
    protected double[] residuals;
    protected double residualStdDev;
    protected double[] standardErrors;
    protected double adjustedR2;
    protected double R2;
    protected double responseVariance;
    protected double responseMean;

    @Override
    public Object clone(){
        GlmStatistics clone = new GlmStatistics();
        clone.copy(this);
        return clone;
    }

    public void copy(GlmStatistics rhs){
        VcovMatrix = rhs.VcovMatrix == null ? null : rhs.VcovMatrix.clone();
        residuals = rhs.residuals == null ? null : rhs.residuals.clone();
        residualStdDev = rhs.residualStdDev;
        standardErrors = rhs.standardErrors == null ? null : rhs.standardErrors.clone();
        adjustedR2 = rhs.adjustedR2;
        R2 = rhs.R2;
        responseVariance = rhs.responseVariance;
        responseMean = rhs.responseMean;
    }


    public GlmStatistics() {

    }

    /// <summary>
    /// In this particular instance, it is assumed that W = sigma^(-2) I, that is e is identical and uncorrelated
    /// </summary>
    /// <param name="A"></param>
    /// <param name="b"></param>
    /// <param name="x"></param>
    public GlmStatistics(double[][] A, double[] b, double[] x) {
        int m = A.length;
        int n = A[0].length;

        residuals = new double[m];

        for (int i = 0; i < m; ++i) {
            double cross_prod = 0;
            for (int j = 0; j < n; ++j) {
                cross_prod += A[i][j] * x[j];
            }
            residuals[i] = b[i] - cross_prod;
        }

        double residual_mu = 0;
        residualStdDev = StdDev.apply(residuals, residual_mu);

        responseMean = Mean.apply(b);
        responseVariance = Math.pow(StdDev.apply(b, responseMean), 2);

        // (A.transpose * A).inverse * sigma^2
        Matrix AtA = new Matrix(n, n);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                double cross_prod = 0;
                for (int k = 0; k < m; ++k) {
                    cross_prod += A[i][k] * A[k][j];
                }
                AtA.set(i, j, cross_prod);
            }
        }

        Matrix AtAInv = AtA.inverse();
        double sigmaSq = residualStdDev * residualStdDev;

        VcovMatrix = new double[n][];
        for (int i = 0; i < n; ++i) {
            VcovMatrix[i] = new double[n];
            for (int j = 0; j < n; ++j) {
                VcovMatrix[i][j] = AtAInv.get(i, j) * sigmaSq;
            }
        }

        standardErrors = new double[n];
        for (int i = 0; i < n; ++i) {
            standardErrors[i] = Math.sqrt(VcovMatrix[i][i]);
        }

        R2 = 1 - sigmaSq / responseVariance;
        adjustedR2 = 1 - sigmaSq / responseVariance * (n - 1) / (n - x.length - 1);

    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="n">Number of variables</param>
    /// <param name="m">Number of shrinkedData points</param>
    public GlmStatistics(int n, int m) {
        VcovMatrix = new double[n][];
        for (int i = 0; i < n; ++i) {
            VcovMatrix[i] = new double[n];
        }

        residuals = new double[m];
        standardErrors = new double[n];
    }

    public double getR2() {
        return R2;
    }

    public void setR2(double value) {
        R2 = value;
    }

    /// <summary>
    /// variance-covariance matrix
    /// </summary>
    public double[][] getVCovMatrix() {
        return VcovMatrix;
    }

    public double[] getResiduals() {
        return residuals;
    }

    public double getResidualStdDev() {
        return residualStdDev;
    }

    public void setResidualStdDev(double value) {
        residualStdDev = value;
    }

    public double getRSS() {
        return residualStdDev * residualStdDev;
    }

    public double[] getStandardErrors() {
        return standardErrors;
    }

    public double getAdjustedR2() {
        return adjustedR2;
    }

    public void setAdjustedR2(double value) {
        adjustedR2 = value;
    }

    public double R2() {
        return R2;
    }

    public double getResponseVariance() {
        return responseVariance;
    }

    public void setResponseVariance(double value) {
        responseVariance = value;
    }

    public double getResponseMean() {
        return responseMean;
    }

    public void setResponseMean(double value) {
        responseMean = value;
    }
}

