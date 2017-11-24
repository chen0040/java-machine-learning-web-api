package com.github.chen0040.ml.gaussianmixture;

import Jama.Matrix;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.statistics.distributions.univariate.NormalDistribution;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class MultiVariateNormalOutliers extends NormalOutliers {
    private static final double DEFAULT_VALUE = 0;
    private double[][] matMu;
    private double[][] matVar;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        MultiVariateNormalOutliers rhs2 = (MultiVariateNormalOutliers)rhs;
        matMu = rhs2.matMu == null ? null : rhs2.matMu.clone();
        matVar = rhs2.matVar == null ? null : rhs2.matVar.clone();
    }

    @Override
    public Object clone(){
        MultiVariateNormalOutliers clone = new MultiVariateNormalOutliers();
        clone.copy(this);

        return clone;
    }

    public MultiVariateNormalOutliers(){
        super();
    }

    public double[][] getMatMu() {
        return matMu;
    }

    public void setMatMu(double[][] matMu) {
        this.matMu = matMu;
    }

    public double[][] getMatVar() {
        return matVar;
    }

    public void setMatVar(double[][] matVar) {
        this.matVar = matVar;
    }

    private Matrix toMatrix(double[][] mat){
        Matrix m = new Matrix(mat.length, mat[0].length);
        for(int i=0; i < mat.length; ++i)
        {
            for(int j=0; j < mat[0].length; ++j){
                m.set(i, j, mat[i][j]);
            }
        }
        return m;
    }

    @Override
    public double calculateProbability(IntelliTuple tuple) {

        Matrix matrixMu = toMatrix(matMu);
        Matrix matrixVar = toMatrix(matVar);

        double[] x = getModelSource().toNumericArray(tuple);

        int n = x.length;
        Matrix X = new Matrix(n, 1);
        for(int k=0; k < n; ++k){
            X.set(k, 0, x[k]);
        }

        double det = matrixVar.det();
        Matrix Var_inverse = matrixVar.inverse();
        Matrix X_minus_mu = X.minus(matrixMu);
        Matrix X_minus_mu_transpose = X_minus_mu.transpose();
        Matrix V = X_minus_mu_transpose.times(Var_inverse).times(X_minus_mu);

        double num2 = Math.pow(2 * Math.PI, n / 2.0) * Math.sqrt(Math.abs(det));

        return Math.exp(-0.5 * V.get(0, 0)) / num2;
    }



    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        boolean autoThresholding = isAutoThresholding();
        setAttribute(AUTO_THRESHOLDING, 0);
        super.batchUpdate(batch);
        setAttribute(AUTO_THRESHOLDING, autoThresholding ? 1 : 0);

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        Matrix matrixMu = new Matrix(dimension, 1);
        Matrix matrixVar = new Matrix(dimension, dimension);

        for(int k=0; k < dimension; ++k){
            NormalDistribution distribution = model.get(k);
            matrixMu.set(k, 0, distribution.getMu());
        }

        int batchSize = batch.tupleCount();


        for(int i=0; i < batchSize; ++i){
            Matrix X = new Matrix(dimension, 1);
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            for(int k=0; k < dimension; ++k){
                X.set(k, 0, x[k]);
            }
            Matrix X_minus_mu = X.minus(matrixMu);
            Matrix X_minus_mu_transpose = X_minus_mu.transpose();

            matrixVar = matrixVar.plus(X_minus_mu.times(X_minus_mu_transpose).times(1.0 / batchSize));
        }

        matMu = new double[matrixMu.getRowDimension()][];
        for(int i=0; i < matrixMu.getRowDimension(); ++i){
            matMu[i] = new double[matrixMu.getColumnDimension()];
            for(int j=0; j < matrixMu.getColumnDimension(); ++j){
                matMu[i][j] = matrixMu.get(i, j);
            }
        }

        matVar = new double[matrixVar.getRowDimension()][];
        for(int i=0; i < matrixVar.getRowDimension(); ++i){
            matVar[i] = new double[matrixVar.getColumnDimension()];
            for(int j=0; j < matrixVar.getColumnDimension(); ++j){
                matVar[i][j] = matrixVar.get(i, j);
            }
        }

        if(isAutoThresholding()){
            adjustThreshold(batch);
        }

        return new BatchUpdateResult();
    }
}
