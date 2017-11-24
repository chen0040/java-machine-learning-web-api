package com.github.chen0040.ml.glm.solvers;

import Jama.Matrix;
import com.github.chen0040.ml.glm.links.LinkFunction;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.statistics.metrics.Mean;
import com.github.chen0040.statistics.metrics.StdDev;
import com.github.chen0040.statistics.metrics.Variance;

/**
 * Created by memeanalytics on 15/8/15.
 */
/// <summary>
/// In regressions, we tried to find a set of model coefficient such for
/// A * x = b + e
///
/// A * x is known as the model matrix, b as the response vector, e is the error terms.
///
/// In OLS (Ordinary Least Square), we assumes that the variance-covariance matrix V(e) = sigma^2 * W, where:
///   W is a symmetric positive definite matrix, and is a diagonal matrix
///   sigma is the standard error of e
///
/// In OLS (Ordinary Least Square), the objective is to find x_bar such that e.transpose * W * e is minimized (Note that since W is positive definite, e * W * e is alway positive)
/// In other words, we are looking for x_bar such as (A * x_bar - b).transpose * W * (A * x_bar - b) is minimized
///
/// Let y = (A * x - b).transpose * W * (A * x - b)
/// Now differentiating y with respect to x, we have
/// dy / dx = A.transpose * W * (A * x - b) * 2
///
/// To find min y, set dy / dx = 0 at x = x_bar, we have
/// A.transpose * W * (A * x_bar - b) = 0
///
/// Transform this, we have
/// A.transpose * W * A * x_bar = A.transpose * W * b
///
/// Multiply both side by (A.transpose * W * A).inverse, we have
/// x_bar = (A.transpose * W * A).inverse * A.transpose * W * b
/// This is commonly solved using IRLS
/// The implementation of Glm based on iteratively re-weighted least squares estimation (IRLS)
///
/// Discussion:
///
/// As inversion is performed for A.transpose * W * A, since A.transpose * W * A may not be directly invertible, the IRLS in this implementation is potentially numerically
/// unstable and not generally advised, use the QR or SVD variant of IRLS instead
///
/// </summary>
public class GlmIrls extends Glm {
    private final static double EPSILON = 1e-20;
    private Matrix A;
    private Matrix b;
    private Matrix At;

    @Override
    public void copy(Glm rhs){
        super.copy(rhs);

        GlmIrls rhs2 = (GlmIrls)rhs;
        A = rhs2.A == null ? null : (Matrix)rhs2.A.clone();
        b = rhs2.b == null ? null : (Matrix)rhs2.b.clone();
        At = rhs2.At == null ? null : (Matrix)rhs2.At.clone();
    }

    @Override
    public Object clone(){
        GlmIrls clone = new GlmIrls();
        clone.copy(this);

        return clone;
    }

    public GlmIrls(){
        super();
    }

    public GlmIrls(GlmDistributionFamily distribution, LinkFunction linkFunc, double[][] A, double[] b)

    {
        super(distribution, linkFunc, null, null, null);
        this.A = toMatrix(A);
        this.b = columnVector(b);
        this.At = this.A.transpose();
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    public GlmIrls(GlmDistributionFamily distribution, double[][] A, double[] b)

    {
        super(distribution);
        this.A = toMatrix(A);
        this.b = columnVector(b);
        this.At = this.A.transpose();
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    private static Matrix toMatrix(double[][] A) {
        int m = A.length;
        int n = A[0].length;

        Matrix Am = new Matrix(m, n);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                Am.set(i, j, (float)A[i][j]);
            }
        }

        return Am;
    }

    private static Matrix columnVector(double[] b) {
        int m = b.length;
        Matrix B = new Matrix(m, 1);
        for (int i = 0; i < m; ++i) {
            B.set(i, 0, b[i]);
        }
        return B;
    }

    private static Matrix columnVector(int n) {
        return new Matrix(n, 1);
    }

    private static Matrix identity(int m) {
        Matrix A = new Matrix(m, m);
        for (int i = 0; i < m; ++i) {
            A.set(i, i, 1);
        }
        return A;
    }

    @Override
    public double[] solve() {
        int m = A.getRowDimension();
        int n = A.getColumnDimension();

        Matrix x = columnVector(n);

        Matrix W = null;
        Matrix AtWAInv = null;

        for (int j = 0; j < maxIters; ++j) {

            Matrix eta = A.times(x); // eta: m x 1
            Matrix z = columnVector(m); //z: m x 1
            double[] g = new double[m];
            double[] gprime = new double[m];

            for (int k = 0; k < m; ++k) {
                g[k] = linkFunc.GetInvLink(eta.get(k, 0));
                gprime[k] = linkFunc.GetInvLinkDerivative(eta.get(k, 0));

                z.set(k, 0, eta.get(k, 0) + (b.get(k, 0) - g[k]) / gprime[k]);
            }

            W = identity(m); // W: m x m
            for (int k = 0; k < m; ++k) {
                double g_variance = getVariance(g[k]);
                if (g_variance == 0) {
                    g_variance = EPSILON;
                }
                W.set(k, k, gprime[k] * gprime[k] / g_variance);
            }

            Matrix x_old = x;

            Matrix AtW = At.times(W); // AtW: n x m

            // solve x for At * W * A * x = At * W * z
            Matrix AtWA = AtW.times(A); // AtWA: n x n
            AtWAInv = AtWA.inverse();
            x = AtWAInv.times(AtW).times(z);

            if ((x.minus(x_old)).norm2() < mTol) {
                break;
            }
        }

        glmCoefficients = new double[n];
        for (int i = 0; i < n; ++i) {
            glmCoefficients[i] = x.get(i, 0);
        }

        updateStatistics(AtWAInv, W);

        return glmCoefficients;
    }

    /// <summary>
    ///
    /// </summary>
    /// <param name="vcovmat">variance-covariance matrix for the model coefficients</param>
    private void updateStatistics(Matrix vcovmat, Matrix W) {
        int n = vcovmat.getRowDimension();
        int m = b.getRowDimension();

        double[] stdErr = mStats.getStandardErrors();
        double[][] VCovMatrix = mStats.getVCovMatrix();
        double[] residuals = mStats.getResiduals();

        for (int i = 0; i < n; ++i) {
            stdErr[i] = Math.sqrt(vcovmat.get(i, i));
            for (int j = 0; j < n; ++j) {
                VCovMatrix[i][j] = vcovmat.get(i, j);
            }
        }

        double[] outcomes = new double[m];
        for (int i = 0; i < m; i++) {
            double cross_prod = 0;
            for (int j = 0; j < n; ++j) {
                cross_prod += A.get(i, j) * glmCoefficients[j];
            }
            residuals[i] = b.get(i, 0) - linkFunc.GetInvLink(cross_prod);
            outcomes[i] = b.get(i, 0);
        }

        mStats.setResidualStdDev(StdDev.apply(mStats.getResiduals(), 0));
        mStats.setResponseMean(Mean.apply(outcomes));
        mStats.setResponseVariance(Variance.apply(outcomes, mStats.getResponseMean()));

        mStats.setR2(1 - mStats.getResidualStdDev() * mStats.getResidualStdDev() / mStats.getResponseVariance());
        mStats.setAdjustedR2(1 - mStats.getResidualStdDev() * mStats.getResidualStdDev() / mStats.getResponseVariance() * (n - 1) / (n - glmCoefficients.length - 1));
    }
}


