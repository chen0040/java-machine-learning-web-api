package com.github.chen0040.ml.linearalg;

import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class MatrixUtils {
    /**
    * @brief Convert a list of column vectors into a matrix
    *
    * @param[List] R
    * @return Matrix
    */
    public static Matrix matrixFromColumnVectors(List<Vector> R)
    {
        int n = R.size();
        int m = R.get(0).getDimension();

        Matrix T = new Matrix(m, n);
        for (int c = 0; c < n; ++c)
        {
            Vector Rcol = R.get(c);
            for (int r : Rcol.getData().keySet())
            {
                T.set(r, c, Rcol.get(r));
            }
        }
        return T;
    }
}
