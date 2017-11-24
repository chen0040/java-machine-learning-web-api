package com.github.chen0040.ml.linearalg.cholesky;

import com.github.chen0040.ml.linearalg.DoubleUtils;
import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.Matrix;

/**
 * Created by chen0469 on 10/13/2015 0013.
 */
public class CholeskyFactorization {
    /** 
     * Decompose a positive definite matrix A into the multiplication of a lower triangular matrix and its tranpose
     * 
     * @param A A is a positive definite matrix of order n
     * @return A lower triangle such that A = L * L.transpose
     */          
    public static Cholesky factorize(Matrix A)
    {
        int n = A.getRowCount();
        if(n != A.getColumnCount()){
            System.err.println("Matrix must have the same number of rows and columns");
            return  null;
        }

        Matrix L = new Matrix(n, n);

        _factorize(A, L, 0, n);
        return new Cholesky(L);
    }

    private static void _factorize(Matrix A, Matrix L, int i, int n)
    {
        if (i == n)
        {
            return;
        }

        int[] subRows = new int[A.getRowCount() - 1];
        int[] subCols = new int[A.getColumnCount() - 1];
        for (int j = i + 1; j < n; ++j)
        {
            subRows[j - i - 1] = j;
        }
        for (int j = i + 1; j < n; ++j)
        {
            subCols[j - i - 1] = j;
        }

        Matrix A_22 = subMatrix(A, subRows, subCols);


        double a_11 = A.get(i, i);
        if (!DoubleUtils.isZero(a_11))
        {
            double l_11 = Math.sqrt(a_11); // l_{11} = sqrt(a_{11})
            L.set(i, i, l_11);

            // L_{21} = A_{21} / l_{11}
            Vector L_21 = new Vector();
            L_21.setId(i);
            for (int j = i + 1; j < n; ++j)
            {
                L_21.set(j, A.get(j, i) / l_11);
                L.set(j, i, L_21.get(j));
            }

            Integer[] kk = (Integer[])L_21.getData().keySet().toArray();
            for (int rk = 0; rk < kk.length; ++rk)
            {
                int row = kk[rk];
                for (int ck = 0; ck < kk.length; ++ck)
                {
                    int col = kk[ck];
                    A_22.set(row, col, A_22.get(row, col) -L_21.get(row) * L_21.get(col));
                }
            }
        }

        _factorize(A_22, L, i + 1, n);
    }

    private static Matrix subMatrix(Matrix A, int[] rows, int[] cols) {
        Matrix sm = new Matrix();
        for (int row : rows)
        {
            for (int col : cols)
            {
                sm.set(row, col, A.get(row, col));
            }
        }

        return sm;
    }
}
