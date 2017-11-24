package com.github.chen0040.ml.linearalg.eigens;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */

import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.Matrix;
import com.github.chen0040.ml.linearalg.qr.QR;

import java.util.Map;

/**
 * @brief QR algorithm uses QR factorization to compute the eigen vector decomposition
 * Currently only the basic QR algorithm is implemented, Hessenberg reduction will be introduced in the future
 */
public class EigenVectorFactorization {

    public static UT factorize(Matrix A){
        int K = 100;
        double epsilon = 1e-10;
        return factorize(A, K, epsilon);
    }

    /**
     * Given A which is a n x n symmetric matrix, we want to find U and T
     * such that:
     *   1. A = U * T * U.transpose
     *   2. U is a n x n matrix whose columns are eigen vectors of A (A * x = lambda * x, where lambda is the eigen value, and x is the eigen vector)
     *   3. T is a diagonal matrix whose diagonal entries are the eigen values
     *
     * The method works in the following manner:
     *   1. intialze A_0 = A, U_0 = I
     *   2. iterate for k = 1, ... K, K is termination criteria
     *   3. In each iteration k:
     *      3.1 QR factorization to find Q_k and R_k such that A_{k-1}=Q_k * R_k
     *      3.2 Let A_k = R_k * Q_k
     *      3.3 Let U_k = U_{k-1} * Q_k
     *   4. Set T = A_K, U = U_K
     *
     * Note that U is an orthogonal matrix if A is a symmetric matrix, in other words, if A.transpose = A, then U.inverse = U.transpose
     *
     *  @param[Matrix] A The matrix to be factorized 
     *  @param[Matrix] K maximum number of iterations 
     *  @return (U, T) T is a diagonal matrix whose diagonal entries are the eigen values, U U is a n x n matrix whose columns are eigen vectors of A
     */
    public static UT factorize(Matrix A, int K, double epsilon)
    {
        if(A.getRowCount() != A.getColumnCount()){
            return new UT(new Exception("A must have the same number of rows and columns"));
        }

        int n = A.getRowCount();
         Matrix A_k = (Matrix)A.clone();
         Matrix U_k = Matrix.identity(n);

         Matrix Q_k, R_k;
        for (int k = 1; k <= K; ++k)
        {
            QR QR_k = A_k.QR();
            Q_k = QR_k.getQ();
            R_k = QR_k.getR();

            A_k = R_k.multiply(Q_k);
            U_k = U_k.multiply(Q_k);

            double sum = 0;
            for (Map.Entry<Integer, Vector> rowEntry : A_k.getRows().entrySet())
            {
                Vector rowVec = rowEntry.getValue();
                int rowId = rowEntry.getKey();
                for(Map.Entry<Integer, Double> dataEntry : rowVec.getData().entrySet())
                {
                    int key = dataEntry.getKey();
                    if (key == rowId) {
                        continue;
                    }
                    sum += Math.abs(dataEntry.getValue());
                }
            }
            if (sum <= epsilon)
            {
                break;
            }
        }

        Matrix T = new Matrix(n,n);

        for (int i = 0; i < n; ++i) {
            T.set(i, i, A_k.get(i, i));
        }

        Matrix U = U_k;

        return new UT(U, T);
    }

    public static Matrix invertSymmetricMatrix(Matrix A){
        int K = 100;
        double epsilon = 1e-10;
        return invertSymmetricMatrix(A, K, epsilon);
    }

    /** <summary>
     * Use eigen vector decomposition to invert a real symmetric matrix
     *
     * The method works like this
     * A * A.inverse = I
     * Since A is symmetric, we have U.inverse = U.transpose (i.e. U is an orthogonal matrix, U consists of orthogonal eigenvectors)
     * U * T * U.transpose * A.inverse = I, since A = U * T * U.transpose
     * T * U.transpose * A.inverse = U.inverse
     * U.transpose * A.inverse = T.inverse * U.inverse, since T.inverse = { 1 / lambda_ii } where T = { lambda_ii }
     * A.inverse = U * T.inverse * U.inverse, since U.transpose = U.inverse
     * A.inverse = U * T.inverse * U.transpose, since U.transpose = U.inverse
     *
     * @param[Matrix] A a n x n square matrix
     */
    public static Matrix invertSymmetricMatrix(Matrix A, int K, double epsilon)
    {
        if(A.isSymmetric()){
            System.err.println("A must be symmetric for inversion to work using eigen vector decomposition");
        }

        int n = A.getRowCount();

        UT UT = factorize(A, K, epsilon);
        Matrix T = UT.getT();
        Matrix U = UT.getU();

        Matrix Tinv = new Matrix(n, n);
        for (int i = 0; i < n; ++i)
        {
            double lambda_ii = T.get(i, i);
            if (Math.abs(lambda_ii) < epsilon)
            {
                System.err.println("The matrix is not invertiable");
            }
            Tinv.set(i, i, 1 / lambda_ii);
        }

        Matrix Uinv = U.transpose();
        return U.multiply(Tinv).multiply(Uinv);
    }


}
