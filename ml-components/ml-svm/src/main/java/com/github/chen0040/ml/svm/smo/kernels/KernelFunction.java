package com.github.chen0040.ml.svm.smo.kernels;

import Jama.Matrix;
import com.github.chen0040.ml.svm.KernelFunctionType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by memeanalytics on 13/8/15.
 */
public abstract class KernelFunction {
    protected KernelFunctionType type;
    private double sigma = 0.1;

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public KernelFunctionType getType(){
        return type;
    }

    public KernelFunction(KernelFunctionType type){
        this.type = type;
    }

    public abstract double kernel(Matrix A, Matrix B);

    public double kernel(double a, double b){
        Matrix A = new Matrix(1, 1);
        Matrix B = new Matrix(1, 1);

        A.set(0, 0, a);
        B.set(0, 0, b);

        return kernel(A, B);
    }

    public KernelFunction clone(){
        throw new NotImplementedException();
    }


    public void copy(KernelFunction rhs){
        type = rhs.type;
        sigma = rhs.sigma;
    }
}
