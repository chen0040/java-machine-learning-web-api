package com.github.chen0040.ml.svm.smo.models;

import Jama.Matrix;
import com.github.chen0040.ml.svm.smo.kernels.KernelFunction;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class SMOSVMModel {
    public Matrix X;
    public Matrix y;
    public KernelFunction kernelFunction;
    public double b; //threshold
    public Matrix alphas; //Lagrange multipliers
    public Matrix w; //

    public void copy(SMOSVMModel rhs){
        X = clone(rhs.X);
        y = clone(rhs.y);
        kernelFunction = clone(rhs.kernelFunction);
        b = rhs.b;
        alphas = clone(rhs.alphas);
        w = clone(rhs.w);
    }

    @Override
    public Object clone(){
        SMOSVMModel clone = new SMOSVMModel();
        clone.copy(this);

        return clone;
    }

    private KernelFunction clone(KernelFunction k){
        return k==null ? null : k.clone();
    }

    private Matrix clone(Matrix A){
        return A==null ? null : (Matrix)A.clone();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Light SVM Model: \n");

        sb.append("\n");

        sb.append("X = \n");
        for(int i=0; i < X.getRowDimension(); ++i){
            for(int j=0; j < X.getColumnDimension(); ++j){
                sb.append(String.format("\t%.6f", X.get(i, j)));
            }
            sb.append("\n");
        }

        sb.append("\n");

        sb.append("y = \n");
        for(int i=0; i < y.getRowDimension(); ++i){
            for(int j=0; j < y.getColumnDimension(); ++j){
                sb.append(String.format("\t%f", y.get(i, j)));
            }
            sb.append("\n");
        }

        sb.append("\n");

        sb.append("kernelFunction = "+kernelFunction.getType()+"\n");

        sb.append(String.format("b = %.3f\n", b));

        sb.append("alphas = \n");
        for(int i=0; i < alphas.getRowDimension(); ++i){
            for(int j=0; j < alphas.getColumnDimension(); ++j){
                sb.append(String.format("\t%f", alphas.get(i, j)));
            }
            sb.append("\n");
        }

        sb.append("\n");

        sb.append("alphas = \n");
        for(int i=0; i < alphas.getRowDimension(); ++i){
            for(int j=0; j < alphas.getColumnDimension(); ++j){
                sb.append(String.format("\t%f", alphas.get(i, j)));
            }
            sb.append("\n");
        }

        sb.append("w = \n");
        for(int i=0; i < w.getRowDimension(); ++i){
            for(int j=0; j < w.getColumnDimension(); ++j){
                sb.append(String.format("\t%f", w.get(i, j)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
