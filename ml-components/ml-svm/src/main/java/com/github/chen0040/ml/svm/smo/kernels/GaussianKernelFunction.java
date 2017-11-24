package com.github.chen0040.ml.svm.smo.kernels;

import Jama.Matrix;
import com.github.chen0040.ml.svm.KernelFunctionType;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class GaussianKernelFunction extends KernelFunction{

    public GaussianKernelFunction(){
        super(KernelFunctionType.RBF);
    }

    public GaussianKernelFunction(double sigma){
        super(KernelFunctionType.RBF);
        setSigma(sigma);
    }

    // Assumes A and B are column vectors
    @Override
    public double kernel(Matrix A, Matrix B) {
        Matrix r = A.minus(B);
        double rSq = r.transpose().times(r).get(0, 0);
        return Math.exp(- rSq / (2 * getSigma() * getSigma()));
    }

    @Override
    public KernelFunction clone(){
        GaussianKernelFunction clone = new GaussianKernelFunction();
        clone.copy(this);
        return clone;
    }
}
