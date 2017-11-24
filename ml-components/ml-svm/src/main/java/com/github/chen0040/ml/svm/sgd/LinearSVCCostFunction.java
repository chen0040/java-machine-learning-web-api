package com.github.chen0040.ml.svm.sgd;

import com.github.chen0040.op.commons.models.costs.CostFunction;

/**
 * Created by memeanalytics on 13/8/15.
 * http://ttic.uchicago.edu/~nati/Publications/PegasosMPB.pdf
 */
public class LinearSVCCostFunction extends CostFunction {
    protected double[][] X;
    protected int[] Y;

    protected int N;
    protected double C = 1;

    protected double p = 1;

    @Override
    public void copy(CostFunction rhs){
        super.copy(rhs);

        LinearSVCCostFunction rhs2 = (LinearSVCCostFunction)rhs;
        X = rhs2.X == null ? null : rhs2.X.clone();
        Y = rhs2.Y == null ? null : rhs2.Y.clone();
        N = rhs2.N;
        C = rhs2.C;
        p = rhs2.p;
    }

    @Override
    public Object clone(){
        LinearSVCCostFunction clone = new LinearSVCCostFunction();
        clone.copy(this);

        return clone;
    }

    public LinearSVCCostFunction(){

    }

    public LinearSVCCostFunction(double[][] X, int[] Y, int dimension_count, int sample_count) {
        super(dimension_count, -10, 10);
        this.X = X;
        this.Y = Y;
        this.N = sample_count;
    }



    @Override
    public double evaluate(double[] theta) {
        double J = 0;

        for (int i = 0; i < N; ++i) {
            double z = 0;
            for (int d2 = 0; d2 < dimensionCount; ++d2) {
                z += (theta[d2] * X[i][d2]);
            }
            J += C * L(Y[i], z);
        }

        J /= N;

        for (int d = 1; d < dimensionCount; ++d) {
            J += theta[d] * theta[d] / 2;
        }

        //System.out.println("J: "+J);

        return J;
    }

    @Override
    public void calcGradient(double[] theta, double[] grad) {
        for(int i=0; i < dimensionCount; ++i){
            grad[i] = 0;
        }

        for(int i=0; i < N; ++i){

            double z = 0;
            for (int d2 = 0; d2 < dimensionCount; ++d2) {
                z += (theta[d2] * X[i][d2]);
            }
            if(Y[i] * z < 1){
                for(int d2 = 0; d2 < dimensionCount; ++d2) {
                    grad[d2] += (theta[d2] - C * Y[i] * X[i][d2]);
                }
            } else {
                for(int d2 = 0; d2 < dimensionCount; ++d2){
                    grad[d2] += theta[d2];
                }
            }
        }

        for(int i=0; i < dimensionCount; ++i){
            grad[i] /= N;
        }
    }

    private double L(double y, double t){
        double l = Math.pow(Math.max(0, 1 - y * t), p);
        return l;
    }
}

