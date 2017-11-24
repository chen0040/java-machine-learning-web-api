package com.github.chen0040.ml.glm.solvers;

import Jama.CholeskyDecomposition;
import Jama.Matrix;
import Jama.SingularValueDecomposition;
import com.github.chen0040.ml.glm.links.LinkFunction;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.statistics.metrics.StdDev;
import com.github.chen0040.statistics.metrics.Variance;
import com.github.chen0040.statistics.metrics.Mean;

/**
 * Created by memeanalytics on 15/8/15.
 */
/// <summary>
/// The implementation of Glm based on IRLS SVD Newton variant
///
/// If you are really concerned about the rank deficiency of the model matrix A and the QR New variant does not a rank-revealing QR method
/// , then SVD Newton variant IRLS can be used (albeit slower than the QR variant).
///
/// The SVD-based method is adapted from the QR variant to used SVD to definitively determines the rank of the model matrix.
///
/// Note that SVD is also potentially much stable compare to the basic IRLS as it uses pseudo inverse
/// Note that SVD is slower if m >> n since it involves  m dimension multiplication and the SVD for large m is costly
/// </summary>
public class GlmIrlsSvdNewton extends Glm {
    private static final double EPSILON = 1e-34;
    private Matrix A;
    private Matrix b;
    private Matrix At;

    @Override
    public void copy(Glm rhs){
        super.copy(rhs);

        GlmIrlsSvdNewton rhs2 = (GlmIrlsSvdNewton)rhs;
        A = rhs2.A == null ? null : (Matrix)rhs2.A.clone();
        b = rhs2.b == null ? null : (Matrix)rhs2.b.clone();
        At = rhs2.At == null ? null : (Matrix)rhs2.At.clone();
    }

    @Override
    public Object clone(){
        GlmIrlsSvdNewton clone = new GlmIrlsSvdNewton();
        clone.copy(this);

        return clone;
    }

    public GlmIrlsSvdNewton(){

    }

    public GlmIrlsSvdNewton(GlmDistributionFamily distribution, LinkFunction linkFunc, double[][] A, double[] b)
    {
        super(distribution, linkFunc, null, null, null);
        this.A = new Matrix(A);
        this.b = columnVector(b);
        this.At = this.A.transpose();
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    public GlmIrlsSvdNewton(GlmDistributionFamily distribution, double[][] A, double[] b)
    {
        super(distribution);
        this.A = new Matrix(A);
        this.b = columnVector(b);
        this.At = this.A.transpose();
        this.mStats = new GlmStatistics(A[0].length, b.length);
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

    @Override
    public double[] solve() {
        int m = A.getRowDimension();
        int n = A.getColumnDimension();

        int m2 = Math.min(m, n);

        Matrix t = columnVector(m);

        Matrix s = columnVector(n);
        Matrix sy = columnVector(n);
        Matrix s_old;

        SingularValueDecomposition svd = A.svd();
        Matrix U = svd.getU(); // U is a m x m orthogonal matrix
        Matrix V = svd.getV(); // V is a n x n orthogonal matrix
        Matrix Sigma = svd.getS(); // Sigma is a m x n diagonal matrix with non-negative real numbers on its diagonal


        Matrix Ut = U.transpose();


        //SigmaInv is obtained by replacing every non-zero diagonal entry by its reciprocal and transposing the resulting matrix
        Matrix SigmaInv = new Matrix(m2, m2);
        for (int i = 0; i < m2; ++i) // assuming m >= n
        {
            double sigma_i = Sigma.get(i, i);
            if (sigma_i < EPSILON) // model matrix A is rank deficient
            {
                System.out.println("Near rank-deficient model matrix");
                return null;
            }
            SigmaInv.set(i, i, 1.0 / sigma_i);
        }
        SigmaInv = SigmaInv.transpose();

        double[] W = new double[m];

        for (int j = 0; j < maxIters; ++j) {
            Matrix z = columnVector(m);
            double[] g = new double[m];
            double[] gprime = new double[m];

            for (int k = 0; k < m; ++k) {
                g[k] = linkFunc.GetInvLink(t.get(k, 0));
                gprime[k] = linkFunc.GetInvLinkDerivative(t.get(k, 0));

                z.set(k, 0, t.get(k, 0) + (b.get(k, 0) - g[k]) / gprime[k]);
            }

            int tiny_weight_count = 0;
            for (int k = 0; k < m; ++k) {
                double w_kk = gprime[k] * gprime[k] / getVariance(g[k]);
                W[k] = w_kk;
                if (w_kk < EPSILON * 2) {
                    tiny_weight_count++;
                }
            }

            if (tiny_weight_count > 0) {
                System.out.println("Warning: tiny weights encountered, (diag(W)) is too small");
            }

            s_old = s;

            Matrix UtW = new Matrix(m2, m);
            for (int k = 0; k < m2; ++k) {
                for (int k2 = 0; k2 < m; ++k2) {
                    UtW.set(k, k2, Ut.get(k, k2) * W[k2]);
                }
            }

            Matrix UtWU = UtW.times(U); // m x m positive definite matrix
            CholeskyDecomposition cholesky = UtWU.chol();

            Matrix L = cholesky.getL(); // m x m lower triangular matrix

            Matrix Lt = L.transpose(); // m x m upper triangular matrix

            Matrix UtWz = UtW.times(z); // m x 1 vector

            // (Ut * W * U) * s = Ut * W * z
            // L * Lt * s = Ut * W * z (Cholesky factorization on Ut * W * U)
            // L * sy = Ut * W * z, Lt * s = sy
            s = columnVector(n);
            for (int i = 0; i < n; ++i) {
                s.set(i, 0, 0);
                sy.set(i, 0, 0);
            }

            // forward solve sy for L * sy = Ut * W * z
            for (int i = 0; i < n; ++i)  // since m >= n
            {
                double cross_prod = 0;
                for (int k = 0; k < i; ++k) {
                    cross_prod += L.get(i, k) * sy.get(k, 0);
                }
                sy.set(i, 0, (UtWz.get(i, 0) - cross_prod) / L.get(i, i));
            }
            // backward solve s for Lt * s = sy
            for (int i = n - 1; i >= 0; --i) {
                double cross_prod = 0;
                for (int k = i + 1; k < n; ++k) {
                    cross_prod += Lt.get(i, k) * s.get(k, 0);
                }
                s.set(i, 0, (sy.get(i, 0) - cross_prod) / Lt.get(i, i));
            }


            t = U.times(s);

            if ((s_old.minus(s)).norm2() < mTol) {
                break;
            }
        }

        Matrix x = V.times(SigmaInv).times(Ut).times(t);

        glmCoefficients = new double[n];
        for (int i = 0; i < n; ++i) {
            glmCoefficients[i] = x.get(i, 0);
        }

        updateStatistics(W);

        return getCoefficients();
    }

    public double[] getCoefficients() {
        return glmCoefficients;
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

