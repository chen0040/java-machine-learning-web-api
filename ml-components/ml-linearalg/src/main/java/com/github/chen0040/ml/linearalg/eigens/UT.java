package com.github.chen0040.ml.linearalg.eigens;

import com.github.chen0040.ml.linearalg.Matrix;
import com.github.chen0040.ml.linearalg.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class UT {
    private Exception error;
    private Matrix U;
    private Matrix T;

    public UT(Exception error){
        this.error = error;
    }

    public UT(Matrix U, Matrix T){
        this.U = U;
        this.T = T;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error){
        this.error = error;
    }

    public Matrix getU() {
        return U;

    }

    public Matrix getT() {
        return T;
    }

    public void setU(Matrix U){
        this.U = U;
    }

    public void setT(Matrix T){
        this.T = T;
    }

    public List<Vector> eigenVectors(){
        List<Vector> eigenVectors = U.columnVectors();
        return eigenVectors;
    }

    public List<Double> eigenValues(){
        List<Double> eigenValues=new ArrayList<Double>();

        int n = T.getRowCount();
        for(int i=0; i < n; ++i) {
            eigenValues.add(T.get(i, i));
        }
        return eigenValues;
    }
}
