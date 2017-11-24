package com.github.chen0040.ml.linearalg.qr;

import com.github.chen0040.ml.linearalg.*;
import com.github.chen0040.ml.linearalg.*;

import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class QRFactorization {
    /**
    * @brief Decompose A = Q * R
    *
    * @param[Matrix] A A m x n matrix, m >= n
    * @return Q A m x n orthogonal matrix, this is a column orthonormal matrix and has a property that Q.transpose * Q = I as the column vectors in Q are orthogonal normal vectors
    * @return R A n x n matrix, which is upper triangular matrix if A is invertiable
    */
    public static QR factorize(Matrix A)
    {
        List<Vector> Acols = A.columnVectors();

        Tuple2<List<Vector>, List<Vector>> result = Orthogonalization.orthogonalize2(Acols);
        List<Vector> vstarlist = result.getItem1();
        List<Vector> Rcols = result.getItem2();

        Tuple2<List<Vector>, List<Double>> result2 = VectorUtils.normalize(vstarlist);
        List<Double> norms = result2.getItem2();
        List<Vector> qlist = result2.getItem1();

        Matrix Q = MatrixUtils.matrixFromColumnVectors(qlist);
        Matrix R = MatrixUtils.matrixFromColumnVectors(Rcols);

        Iterable<Integer> rowIndices = R.getRows().keySet();
        for(Integer r : rowIndices)
        {
            Vector row = R.getRow(r);
            R.setRow(r, row.multiply(norms.get(r)));
        }

        return new QR(Q, R);
    }
}
