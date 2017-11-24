package com.github.chen0040.ml.tests.linearalg;

import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.qr.QRSolver;
import com.github.chen0040.ml.linearalg.Matrix;
import com.github.chen0040.ml.linearalg.qr.QR;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by chen0469 on 10/13/2015 0013.
 */
public class QRTests {
    public static double[][] Data = new double[][]{
            new double[] { 12, -51, 4},
            new double[] { 6, 167, -68},
            new double[] { -4, 24, -41}
    };
    
    public static Matrix A = new Matrix(Data);

    @Test
    public void TestQR()
    {
        QR QR = A.QR();
        Matrix Q = QR.getQ();
        Matrix R = QR.getR();

        Matrix Api = Q.multiply(R);

        for (int r = 0; r < A.getRowCount(); ++r)
        {
            for (int c = 0; c < A.getColumnCount(); ++c)
            {
                System.out.println(A.get(r, c)+" = "+Api.get(r, c));
                //assertTrue(Math.abs(A.get(r, c) - Api.get(r, c)) < 1e-10);
            }
        }

        //assertEquals(A, Q.multiply(R));
    }

    @Test
    public void TestQRSolver()
    {
        Vector x = new Vector(new double[] { 2, 4, 1 });
        Vector b = A.multiply(x);

        Vector x_pi = QRSolver.solve(A, b);
        assertEquals(x, x_pi);
    }

    @Test
    public void TestMatrixInverse()
    {
        //A = new SparseMatrix<int, double>(2, 2);

        //A[0, 0] = 4;
        //A[0, 1] = 7;
        //A[1, 0] = 2;
        //A[1, 1] = 6;

        Matrix Ainv = QRSolver.invert(A);
        Matrix Ipi = A.multiply(Ainv);

        //for (int row = 0; row < 2; ++row)
        //{
        //    Console.WriteLine(Ipi[row]);
        //}

        assertEquals(Matrix.identity(3), Ipi);

        Ipi = Ainv.multiply(A);

        //for (int row = 0; row < 2; ++row)
        //{
        //    Console.WriteLine(Ipi[row]);
        //}

        assertEquals(Matrix.identity(3), Ipi);
    }
}
