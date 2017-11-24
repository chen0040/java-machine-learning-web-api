package com.github.chen0040.ml.linearalg.cholesky;

import com.github.chen0040.ml.linearalg.Matrix;

/**
 * Created by chen0469 on 10/13/2015 0013.
 */
public class Cholesky {
    private Matrix L;
    public Cholesky(Matrix L){
        this.L = L;
    }

    private Matrix getL(){
        return L;
    }
}
