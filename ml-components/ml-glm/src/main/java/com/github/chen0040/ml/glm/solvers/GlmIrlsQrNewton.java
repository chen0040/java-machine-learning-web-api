package com.github.chen0040.ml.glm.solvers;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import Jama.QRDecomposition;
import com.github.chen0040.ml.glm.links.LinkFunction;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.statistics.metrics.StdDev;
import com.github.chen0040.statistics.metrics.Variance;
import com.github.chen0040.statistics.metrics.Mean;

import java.util.Random;

/**
 * Created by memeanalytics on 15/8/15.
 */
/// <summary>
/// The implementation of Glm based on IRLS QR Newton variant
/// The idea is to compute the QR factorization of matrix matrix A once. The factorization is then used in the IRLS iteration
///
/// QR factorization results in potentially much better numerical stability since no matrix inversion is actually performed
/// The cholesky factorization used to compute s requires W be positive definite matrix (i.e., z * W * z be positive for any vector z),
/// The positive definite matrix requirement can be easily checked by examining the diagonal entries of the weight matrix W
/// </summary>
public class GlmIrlsQrNewton extends Glm {
    private final static double EPSILON = 1e-20;
    private static Random rand = new Random();
    private Matrix A;
    private Matrix b;
    private Matrix At;

    @Override
    public void copy(Glm rhs){
        super.copy(rhs);

        GlmIrlsQrNewton rhs2 = (GlmIrlsQrNewton)rhs;
        A = rhs2.A == null ? null : (Matrix)rhs2.A.clone();
        b = rhs2.b == null ? null : (Matrix)rhs2.b.clone();
        At = rhs2.At == null ? null : (Matrix)rhs2.At.clone();
    }

    @Override
    public Object clone(){
        GlmIrlsQrNewton clone = new GlmIrlsQrNewton();
        clone.copy(this);

        return clone;
    }

    public GlmIrlsQrNewton(){

    }

    public GlmIrlsQrNewton(GlmDistributionFamily distribution, LinkFunction linkFunc, double[][] A, double[] b)

    {
        super(distribution, linkFunc, null, null, null);
        this.A = toMatrix(A);
        this.b = columnVector(b);
        this.At = this.A.transpose();
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    public GlmIrlsQrNewton(GlmDistributionFamily distribution, double[][] A, double[] b) {
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
                Am.set(i, j, A[i][j]);
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

        Matrix s = columnVector(n);
        Matrix sy = columnVector(n);
        for(int i=0; i < n; ++i){
            s.set(i, 0, 0);
        }

        Matrix t = columnVector(m);
        for(int i=0; i < m; ++i){
            t.set(i, 0, 0);
        }

        double[] g = new double[m];
        double[] gprime = new double[m];

        QRDecomposition qr = A.qr();

        Matrix Q = qr.getQ();
        Matrix R = qr.getR(); // A is m x n, Q is m x n orthogonal matrix, R is n x n (R will be upper triangular matrix if m == n)

        Matrix Qt = Q.transpose();

        double[] W = null;
        for (int j = 0; j < maxIters; ++j) {
            Matrix z = columnVector(m);

            for (int k = 0; k < m; ++k) {
                g[k] = linkFunc.GetInvLink(t.get(k, 0));
                gprime[k] = linkFunc.GetInvLinkDerivative(t.get(k, 0));

                z.set(k, 0, t.get(k, 0) + (b.get(k, 0) - g[k]) / gprime[k]);
            }

            W = new double[m];
            double w_kk_min = Double.MAX_VALUE;
            for (int k = 0; k < m; ++k) {
                double g_variance = getVariance(g[k]);
                double w_kk = gprime[k] * gprime[k] / (g_variance);
                W[k] = w_kk;
                w_kk_min = Math.min(w_kk, w_kk_min);
            }

            if (w_kk_min < Math.sqrt(EPSILON)) {
                System.out.println("Warning: Tiny weights encountered, min(diag(W)) is too small");
            }

            Matrix s_old = s;


            Matrix WQ = new Matrix(m, n); // W * Q
            Matrix Wz = columnVector(m); // W * z
            for (int k = 0; k < m; k++) {
                Wz.set(k, 0, z.get(k, 0) * W[k]);
                for (int k2 = 0; k2 < n; ++k2) {
                    WQ.set(k, k2, Q.get(k, k2) * W[k]);
                }
            }

            Matrix QtWQ = Qt.times(WQ); // a n x n positive definite matrix, therefore can apply Cholesky
            Matrix QtWz = Qt.times(Wz);

            CholeskyDecomposition cholesky = QtWQ.chol();
            Matrix L = cholesky.getL();
            Matrix Lt = L.transpose();

            // (Qt * W * Q) * s = Qt * W * z;
            // L * Lt * s = Qt * W * z (Cholesky factorization on Qt * W * Q)
            // L * sy = Qt * W * z, Lt * s = sy
            // Now forward solve sy for L * sy = Qt * W * z
            // Now backward solve s for Lt * s = sy
            s = columnVector(n);
            for (int i = 0; i < n; ++i) {
                s.set(i, 0, 0);
                sy.set(i, 0, 0);
            }

            //forward solve sy for L * sy = Qt * W * z
            //Console.WriteLine(L);
            for (int i = 0; i < n; ++i) {
                double cross_prod = 0;
                for (int k = 0; k < i; ++k) {
                    cross_prod += L.get(i, k) * sy.get(k, 0);
                }
                sy.set(i, 0, (QtWz.get(i, 0) - cross_prod) / L.get(i, i));
            }
            //backward solve s for U * s = sy
            for (int i = n - 1; i >= 0; --i) {
                double cross_prod = 0;
                for (int k = i + 1; k < n; ++k) {
                    cross_prod += Lt.get(i, k) * s.get(k, 0);
                }
                s.set(i, 0, (sy.get(i, 0) - cross_prod) / Lt.get(i, i));
            }

            t = Q.times(s);



            if ((s_old.minus(s)).norm2() < mTol) {
                break;
            }
        }

        glmCoefficients = new double[n];

        //backsolve x for R * x = Qt * t
        Matrix c = Qt.times(t);

        for (int i = n - 1; i >= 0; --i) // since m >= n
        {
            double cross_prod = 0;
            for (int j = i + 1; j < n; ++j) {
                cross_prod += R.get(i, j) * glmCoefficients[j];
            }
            glmCoefficients[i] = (c.get(i, 0) - cross_prod) / R.get(i, i);
        }


        updateStatistics(W);

        return getCoefficients();

    }

    private Matrix scalarMultiply(Matrix A, double[] v){
        int m = v.length;
        int m2 = A.getRowDimension();
        int n2 = A.getColumnDimension();

        Matrix C = new Matrix(m2, n2);

        if(m == m2){
            for(int i=0; i < m2; ++i){
                for(int j=0; j < n2; ++j){
                    C.set(i, j, A.get(i, j) * v[i]);
                }
            }
        }else if(m == n2){
            for(int i=0; i < n2; ++i){
                for(int j=0; j < m2; ++j){
                    C.set(j, i, A.get(j, i) * v[i]);
                }
            }


        }

        return C;

    }

    protected void updateStatistics(double[] W) {

        Matrix AtWA = scalarMultiply(At, W).times(A);

        Matrix AtWAInv = AtWA.inverse();

        int n = AtWAInv.getRowDimension();
        int m = b.getRowDimension();

        double[] stdErrors = mStats.getStandardErrors();
        double[][] VCovMatrix = mStats.getVCovMatrix();
        double[] residuals = mStats.getResiduals();

        for (int i = 0; i < n; ++i) {
            stdErrors[i] = Math.sqrt(AtWAInv.get(i, i));
            for (int j = 0; j < n; ++j) {
                VCovMatrix[i][j] = AtWAInv.get(i, j);
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

        mStats.setResidualStdDev(StdDev.apply(residuals, 0));
        mStats.setResponseMean(Mean.apply(outcomes));
        mStats.setResponseVariance(Variance.apply(outcomes, mStats.getResponseMean()));

        mStats.setR2(1 - mStats.getResidualStdDev() * mStats.getResidualStdDev() / mStats.getResponseVariance());
        mStats.setAdjustedR2(1 - mStats.getResidualStdDev() * mStats.getResidualStdDev() / mStats.getResponseVariance() * (n - 1) / (n - glmCoefficients.length - 1));
    }
}

