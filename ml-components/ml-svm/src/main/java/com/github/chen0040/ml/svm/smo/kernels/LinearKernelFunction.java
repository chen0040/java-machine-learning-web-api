package com.github.chen0040.ml.svm.smo.kernels;

import Jama.Matrix;
import com.github.chen0040.ml.svm.KernelFunctionType;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class LinearKernelFunction extends KernelFunction {

    public LinearKernelFunction(){
        super(KernelFunctionType.LINEAR);
    }

    // assume A and B are column vectors
    @Override
    public double kernel(Matrix A, Matrix B) {
        return A.transpose().times(B).get(0, 0);
    }

    @Override
    public KernelFunction clone(){
        LinearKernelFunction clone = new LinearKernelFunction();
        clone.copy(this);
        return clone;
    }
}
