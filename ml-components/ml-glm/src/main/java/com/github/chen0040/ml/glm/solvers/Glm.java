package com.github.chen0040.ml.glm.solvers;

import com.github.chen0040.ml.glm.links.*;
import com.github.chen0040.op.commons.OPModule;
import com.github.chen0040.op.commons.services.MatrixOp;
import com.github.chen0040.ml.glm.links.*;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.costs.GradientEvaluationMethod;
import com.github.chen0040.op.commons.models.misc.TerminationEvaluationMethod;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.search.cgs.NonlinearCGSearch;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Random;

/**
 * Created by memeanalytics on 14/8/15.
 */
/// <summary>
/// Link: http://bwlewis.github.io/GLM/
/// GLM is generalized linear model for exponential family of distribution model b = g(a).
/// g(a) is the inverse link function.
///
/// Therefore, for a regressions characterized by inverse link function g(a), the regressions problem be formulated
/// as we are looking for model coefficient set x in/**/
/// g(A * x) = b + e
/// And the objective is to find x such for the following objective:
/// min (g(A * x) - b).transpose * W * (g(A * x) - b)
///
/// Suppose we assumes that e consist of uncorrelated naive variables with identical variance, then W = sigma^(-2) * I,
/// and The objective min (g(A * x) - b) * W * (g(A * x) - b).transpose is reduced to the OLS form:
/// min || g(A * x) - b ||^2
/// </summary>
public class Glm implements Cloneable {
    private static Random random = new Random();
    protected LinkFunction linkFunc;
    protected int maxIters = 25;
    protected double mTol = 0.000001;
    protected double mRegularizationLambda = 0;
    protected GlmDistributionFamily mDistributionFamily;
    protected GlmStatistics mStats = new GlmStatistics();
    protected TerminationEvaluationMethod shouldTerminate = (state, iteration) -> {
        if (!state.improved() || state.improvement() < mTol) {
            return false;
        }
        return iteration >= maxIters;
    };
    protected double[] glmCoefficients;
    private OPModule solver;
    private double[][] A; //first column of A corresponds to x_0 = 1
    private double[] b;
    protected CostEvaluationMethod evaluateCost = new CostEvaluationMethod() {
        public double apply(double[] x, double[] lowerBounds, double[] upperBounds, Object constraint) {
            int m = b.length;
            int n = x.length;

            double[] c = MatrixOp.Multiply(A, x);
            double crossprod = 0;
            for (int i = 0; i < m; ++i) {
                double g = linkFunc.GetInvLink(c[i]);
                double gprime = linkFunc.GetInvLinkDerivative(c[i]);

                double d = g - b[i];
                crossprod += d * d;
            }

            double J = crossprod / (2 * m);

            for (int j = 1; j < n; ++j) {
                J += (mRegularizationLambda * x[j] * x[j]) / (2 * m);
            }

            return J;
        }
    };

    protected GradientEvaluationMethod evaluateGradient = new GradientEvaluationMethod() {
        public void apply(double[] x, double[] gradx, double[] lowerBounds, double[] upperBounds, Object constraint) {
            int m = b.length;
            int n = A[0].length;

            double[] c = MatrixOp.Multiply(A, x);

            double[] g = new double[m];
            double[] gprime = new double[m];
            for (int j = 0; j < m; ++j) {
                g[j] = linkFunc.GetInvLink(c[j]);
                gprime[j] = linkFunc.GetInvLinkDerivative(c[j]);
            }

            for (int i = 0; i < n; ++i) {
                double crossprod = 0;
                for (int j = 0; j < m; ++j) {
                    double cb = g[j] - b[j];
                    crossprod += cb * gprime[j] * A[j][i];
                }

                gradx[i] = crossprod / m;

                if (i != 0) {
                    gradx[i] += (mRegularizationLambda * x[i]) / m;
                }
            }
            /*
            GradientEstimation.CalcGradient(x, gradx, (x2, constraints2) =>
                {
                    return EvaluateCost(x2, lower_bounds, upper_bounds, constraints2);
                });*/
        }
    };

    public Glm(){

    }

    public Glm(GlmDistributionFamily distribution, LinkFunction linkFunc, double[][] A, double[] b, OPModule solver) {
        this.mDistributionFamily = distribution;
        this.solver = solver;
        this.linkFunc = linkFunc;
        this.A = A;
        this.b = b;
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    public Glm(GlmDistributionFamily distribution, double[][] A, double[] b, OPModule solver) {
        this.solver = solver;
        this.mDistributionFamily = distribution;
        this.linkFunc = getLinkFunction(distribution);
        this.A = A;
        this.b = b;
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }


    public Glm(GlmDistributionFamily distribution, double[][] A, double[] b) {
        this.solver = new NonlinearCGSearch();
        this.mDistributionFamily = distribution;
        this.linkFunc = getLinkFunction(distribution);
        this.A = A;
        this.b = b;
        this.mStats = new GlmStatistics(A[0].length, b.length);
    }

    public Glm(GlmDistributionFamily distribution) {
        this.linkFunc = getLinkFunction(distribution);
        this.mDistributionFamily = distribution;
    }

    public Glm(GlmDistributionFamily distribution, double[][] A, double[] b, OPModule solver, int maxIters) {
        this.solver = solver;
        this.mDistributionFamily = distribution;
        this.linkFunc = getLinkFunction(distribution);

        int m = A.length;
        int n = A[0].length;

        this.A = new double[m][];
        for (int i = 0; i < m; i++) {
            this.A[i] = new double[n];
            for (int j = 0; j < n; j++) {
                this.A[i][j] = A[i][j];
            }
        }
        this.b = b;
        if (maxIters > 0) {
            this.maxIters = maxIters;
        }
        this.mStats = new GlmStatistics(m, b.length);
    }

    public static LinkFunction getLinkFunction(GlmDistributionFamily distribution) {
        switch (distribution) {
            case Bernouli:
            case Binomial:
            case Categorical:
            case Multinomial:
                return new LogitLinkFunction();
            case Exponential:
            case Gamma:
                return new InverseLinkFunction();
            case InverseGaussian:
                return new InverseSquaredLinkFunction();
            case Normal:
                return new IdentityLinkFunction();
            case Poisson:
                return new LogLinkFunction();
            default:
                throw new NotImplementedException();
        }
    }

    private LinkFunction clone(LinkFunction rhs){
        if(rhs==null) return null;
        AbstractLinkFunction rhs2 = (AbstractLinkFunction)rhs;
        return (LinkFunction)rhs2.clone();
    }

    public void copy(Glm rhs){
        linkFunc = (rhs.linkFunc);
        maxIters = rhs.maxIters;
        mTol = rhs.mTol;
        mRegularizationLambda = rhs.mRegularizationLambda;
        mDistributionFamily = rhs.mDistributionFamily;
        mStats = rhs.mStats == null ? null : (GlmStatistics)rhs.mStats.clone();

        mDistributionFamily = rhs.mDistributionFamily;

        shouldTerminate = rhs.shouldTerminate;

        glmCoefficients = rhs.glmCoefficients == null ? null : rhs.glmCoefficients.clone();

        solver = rhs.solver== null ? null : (OPModule)rhs.solver.clone();

        A = rhs.A == null ? null : rhs.A.clone(); //first column of A corresponds to x_0 = 1
        b = rhs.b == null ? null : rhs.b.clone();

        evaluateCost = rhs.evaluateCost;
        evaluateGradient = rhs.evaluateGradient;
    }

    @Override
    public Object clone(){
        Glm clone = new Glm();
        clone.copy(this);
        return clone;
    }

    public double getTol() {
        return mTol;
    }

    public void setTol(double value) {
        mTol = value;
    }

    public GlmDistributionFamily getDistributionFamily() {
        return mDistributionFamily;
    }

    public double predict(double[] input_0) {

        if(glmCoefficients == null){
            return Double.NaN;
        }

        int n = input_0.length;

        double linear_predictor = 0;
        for (int i = 0; i < n; ++i) {
            linear_predictor += glmCoefficients[i] * input_0[i];
        }
        return linkFunc.GetInvLink(linear_predictor);
    }

    protected double getVariance(double g) {
        switch (mDistributionFamily) {
            case Bernouli:
            case Binomial:
            case Categorical:
            case Multinomial:
                return g * (1 - g);
            case Exponential:
            case Gamma:
                return g * g;
            case InverseGaussian:
                return g * g * g;
            case Normal:
                return 1;
            case Poisson:
                return g;
            default:
                throw new NotImplementedException();
        }
    }

    public int getMaxIters() {
        return maxIters;
    }

    public void setMaxIters(int value) {
        maxIters = value;
    }

    public double[] getCoefficients() {
        return glmCoefficients;
    }

    public GlmStatistics getStatistics() {
        return mStats;
    }

    public double[] solve() {
        int n = A[0].length;


        double[] x_0 = new double[n];
        for (int i = 0; i < n; ++i) {
            x_0[i] = random.nextDouble();
        }

        NumericSolution s = solver.minimize(x_0, evaluateCost, evaluateGradient, shouldTerminate, null);

        glmCoefficients = s.values();

        updateStatistics();

        return getCoefficients();
    }

    private void updateStatistics() {
        mStats = new GlmStatistics(A, b, glmCoefficients);
    }
}

