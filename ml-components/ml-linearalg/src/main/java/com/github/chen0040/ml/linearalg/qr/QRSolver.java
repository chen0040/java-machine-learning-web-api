package com.github.chen0040.ml.linearalg.qr;

import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.solvers.BackwardSubstitution;
import com.github.chen0040.ml.linearalg.Matrix;
import com.github.chen0040.ml.linearalg.MatrixUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class QRSolver {
     /** <summary>
     * solve x for Ax = b, where A is a invertible matrix
     *
     * The method works as follows:
     *  1. QR factorization A = Q * R
     *  2. Let: c = Q.transpose * b, we have R * x = c, where R is a triangular matrix if A is n x n
     *  3. solve x using backward substitution
     *
     * @param[Matrix] A An m x n matrix with linearly independent columns, where m >= n
     * @param[Matrix] b An m x 1 column vector 
     * return An n x 1 column vector, x, such that Ax = b when m = n and Ax ~ b when m > n 
      */                
    public static Vector solve(Matrix A, Vector b)
    {
        // Q is a m x m matrix, R is a m x n matrix
        // Q = [Q1 Q2] Q1 is a m x n matrix, Q2 is a m x (m-n) matrix
        // R = [R1; 0] R1 is a n x n upper triangular matrix matrix
        // A = Q * R = Q1 * R1
        QR QR = A.QR();
        Matrix Q = QR.getQ();
        Matrix R = QR.getR();

        Vector c = Q.transpose().multiply(b);

        Vector x = BackwardSubstitution.solve(R, c);
        return x;
    }

    /**
     * This is used for data fitting / regression
     * A is a m x n matrix, where m >= n
     * b is a m x 1 column vector
     * The method solves for x, which is a n x 1 column vectors such that A * x is closest to b
     *
     * The method works as follows:
     *   1. Let C = A.transpose * A, we have A.transpose * A * x = C * x = A.transpose * b
     *   2. Decompose C : C = Q * R, we have Q * R * x = A.transpose * b
     *   3. Multiply both side by Q.transpose = Q.inverse, we have Q.transpose * Q * R * x = Q.transpose * A.transpose * b
     *   4. Since Q.tranpose * Q = I, we have R * x = Q.transpose * A.transpose * b
     *   5. solve x from R * x = Q.transpose * A.transpose * b using backward substitution
     *
     * @param[Matrix] A 
     * @param[Matrix] b 
     *
     */
    public static Vector solve_LeastSquare(Matrix A, Vector b)
    {
        Matrix At = A.transpose();
        Matrix C = At.multiply(A); //C is a n x n matrix

        QR QR = C.QR();
        Matrix Q = QR.getQ();
        Matrix R = QR.getR();

        Matrix Qt = Q.transpose();

        Vector d = Qt.multiply(At).multiply(b);

        Vector x = BackwardSubstitution.solve(R, d);

        return x;
    }

    /**
     * This is for finding the least norm in solution set {x | A * x = b}
     * where A is m x n matrix m &lt; n, where for A * x = b has many solutions.
     *
     * The least-norm problem is defined as follows:
     * Objective: min |x|^2
     * Subject to: A * x = b
     *
     * The unique solution to the least norm can be obtained as x_bar = A.tranpose * (A * A.transpose).inverse * b
     * We can solve by the following method
     *   1. solve z such that (A * A.transpose) * z = b
     *   2. x_bar = A.transpose * z
     *
     * @param[Matrix] A 
     * @param[Matrix] b 
     */
    public static Vector solve_LeastNorm(Matrix A, Vector b)
    {
        Matrix At = A.transpose();
        Matrix C = A.multiply(At);
        Vector z = solve(C, b);
        Vector x_bar = At.multiply(z);
        return x_bar;
    }

    public static Matrix invert(Matrix A)
    {
        if(A. getRowCount() != A.getColumnCount()){
            System.err.println("A.rowCount must be equal to A.columnCount to invert");
        }
        int n = A. getRowCount();


        List<Vector> AinvCols = new ArrayList<Vector>();
        for (int i = 0; i < n; ++i)
        {
            Vector e_i = new Vector(n);
            e_i.set(i, 1);
            Vector AinvCol = solve(A, e_i);
            AinvCols.add(AinvCol);
        }

        Matrix Ainv = MatrixUtils.matrixFromColumnVectors(AinvCols);

        return Ainv;
    }


}
