package com.github.chen0040.ml.linearalg.qr;

import com.github.chen0040.ml.linearalg.Matrix;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class QR {
    private Matrix Q;
    private Matrix R;

    public void setQ(Matrix q) {
        Q = q;
    }

    public void setR(Matrix r) {
        R = r;
    }

    public QR(Matrix Q, Matrix R){
        this.Q = Q;
        this.R = R;
    }

    public Matrix getQ() {
        return Q;
    }

    public Matrix getR() {
        return R;
    }
}
