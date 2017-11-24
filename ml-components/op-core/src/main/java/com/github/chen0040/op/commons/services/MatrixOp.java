package com.github.chen0040.op.commons.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by memeanalytics on 14/8/15.
 */
public class MatrixOp
{
    public static double[] ElementWiseAbs(double[] x)
    {
        int n = x.length;
        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = Math.abs(x[i]);
        }
        return result;
    }

    public static double[][] DiagonalMatrix(double[] x)
    {
        int n = x.length;
        double[][] matrix = new double[n][];
        for (int i = 0; i < n; ++i)
        {
            matrix[i] = new double[n];
            matrix[i][i] = x[i];
        }
        return matrix;
    }

    public static double[][] Transpose(double[][] x)
    {
        int rowCount = x.length;
        int colCount = x[0].length;
        double[][] xt = new double[colCount][];
        for (int i = 0; i < colCount; ++i)
        {
            xt[i] = new double[rowCount];
            for (int j = 0; j < rowCount; ++j)
            {
                xt[i][j] = x[j][i];
            }
        }

        return xt;
    }

    public static double[] ElementWiseMinus(double[] x, double[] y)
    {
        int n = x.length;

        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x[i] - y[i];
        }
        return result;
    }

    public static double[] ElementWiseMinus(double x, double[] y)
    {
        int n = y.length;
        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x - y[i];
        }
        return result;
    }

    public static double[] ElementWiseMultiply(double x, double[] y, double[] z)
    {
        int n = y.length;

        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x * y[i] * z[i];
        }

        return result;
    }

    public static double[] ElementWiseMultiply(double x, double[] y)
    {
        int n = y.length;

        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x * y[i];
        }
        return result;
    }

    public static double[] ElementWiseDivide(double[] x, double[] y)
    {
        int n = x.length;

        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x[i] / y[i];
        }

        return result;
    }

    public static double[] ElementWiseAdd(double[] x, double[] y)
    {
        int n = x.length;
        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x[i] - y[i];
        }
        return result;
    }

    public static double[] ElementWiseAdd(double x, double[] y)
    {
        int n = y.length;
        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = x + y[i];
        }

        return result;
    }

    public static double[] ElementWiseExp(double[] x)
    {
        int n = x.length;
        double[] result = new double[n];
        for (int i = 0; i < n; ++i)
        {
            result[i] = Math.exp(x[i]);
        }
        return result;
    }

    public static double[][] Multiply(double[][] A, double[][] B)
    {
        int n = A.length;
        int k = A[0].length;
        int m = B[0].length;

        double[][] result = new double[n][];
        for (int row = 0; row < n; ++row)
        {
            result[row] = new double[m];

            double[] rowA = A[row];
            for (int col = 0; col < m; ++col)
            {
                double sum = 0;
                for (int num = 0; num < k; ++num)
                {
                    sum += rowA[num] * B[num][col];
                }
                result[row][col] = sum;
            }
        }

        return result;
    }

    public static double[] Multiply(double[][] X, double[] beta)
    {
        int n = X.length;
        int k = beta.length;
        double[] result = new double[n];

        for (int i = 0; i < n; ++i)
        {
            result[i] = Multiply(X[i], beta);
        }
        return result;
    }

    public static double Multiply(double[] x, double[] y)
    {
        int n = x.length;
        double sum = 0;
        for (int i = 0; i < n; ++i)
        {
            sum += x[i] * y[i];
        }

        return sum;
    }

    /// <summary>
    /// The method works by using Gaussian elimination to covert the matrix A to a upper triangular matrix, U, and computes the
    /// determinant as the product_i(U_ii) * (-1)^c, where c is the number of row exchange operations that coverts A to U
    /// </summary>
    /// <param name="A">The matrix for which to calculate determinant</param>
    /// <returns>The determinant of A</returns>
    public static double GetDeterminant(double[][] A)
    {
        int ColCount = A[0].length;
        int RowCount = A.length;

        if (RowCount == 2)
        {
            return A[0][0] * A[1][1] - A[0][1] * A[1][0];
        }

        double det = 1;

        UpperTriangularMatrixResult result = GetUpperTriangularMatrix2(A);
        int rowExchangeOpCount = result.getRowExchangeOpCount();
        double[][] C = result.matrix();
        for(int i=0; i < RowCount; ++i)
        {
            det *= C[i][i];
        }

        return det * (rowExchangeOpCount % 2 == 0 ? 1 : -1);
    }

    private static double[][] Clone(double[][] A)
    {
        int rowCount = A.length;
        double[][] clone = new double[rowCount][];
        for (int r = 0; r < rowCount; ++r)
        {
            clone[r] = A[r].clone();
        }
        return clone;
    }


    public static void GaussianElimination(double[][] A, double[][] Q, double[][] M)
    {
        int rowCount = A.length;
        int colCount = A[0].length;


    }

    public static double[][] GetUpperTriangularMatrix(double[][] A)
    {
        UpperTriangularMatrixResult result = GetUpperTriangularMatrix2(A);
        return result.matrix();
    }

    public static class UpperTriangularMatrixResult
    {
        private double[][] upperTriangularMatrix;
        private int rowExchangeOpCount;

        public UpperTriangularMatrixResult(double[][] matrix, int rowExchangeOpCount){
            this.rowExchangeOpCount = rowExchangeOpCount;
            this.upperTriangularMatrix = matrix;
        }

        public double[][] matrix(){
            return upperTriangularMatrix;
        }

        public int getRowExchangeOpCount(){
            return rowExchangeOpCount;
        }
    }

    /// <summary>
    /// The method works by using Gaussian elimination to covert the matrix A to a upper triangular matrix
    /// The computational Complexity is O(n^3)
    /// </summary>
    /// <param name="A">The original matrix</param>
    /// <returns>The upper triangular matrix</returns>
    public static UpperTriangularMatrixResult GetUpperTriangularMatrix2(double[][] A)
    {
        double[][] B = Clone(A);

        int colCount = B[0].length;
        int rowCount = B.length;

        HashSet<Integer> rows_left = new HashSet<Integer>();
        for (int r = 0; r < rowCount; ++r)
        {
            rows_left.add(r);
        }

        List<Integer> row_mapping = new ArrayList<Integer>();
        for (int r = 0; r < rowCount; ++r)
        {
            row_mapping.add(r);
        }

        int rowExchangeOpCount = 0;

        List<Integer> new_rows = new ArrayList<Integer>();
        for (int c = 0; c < colCount; ++c)
        {
            List<Integer> nonzero_rows = GetRowsWithNonZeroAtColIndex(rows_left, B, c);
            if (nonzero_rows.size() > 0)
            {
                int pivot_row = GetPivotRow(nonzero_rows, B, c);

                new_rows.add(pivot_row);
                rows_left.remove(pivot_row);

                for (int i = 0; i < nonzero_rows.size(); ++i)
                {
                    int r = nonzero_rows.get(i);
                    if (r != pivot_row)
                    {
                        double multiplier = B[r][c] / B[pivot_row][c];

                        for (int j = c; j < rowCount; ++j)
                        {
                            B[r][j] -= B[pivot_row][j] * multiplier;
                        }
                    }
                }
            }
        }

        for (int r : rows_left)
        {
            new_rows.add(r);
        }

        for (int i = 0; i < new_rows.size(); ++i)
        {
            int new_row = new_rows.get(i);
            int old_row = i;

            if (new_row != old_row)
            {
                double[] temp = B[new_row];
                B[new_row] = B[old_row];
                B[old_row] = temp;

                int new_row_index = i;
                int old_row_index = new_rows.indexOf(old_row);
                Swap(new_rows, new_row_index, old_row_index);

                rowExchangeOpCount++;
            }
        }


        return new UpperTriangularMatrixResult(B, rowExchangeOpCount);
    }

    private static void Swap(List<Integer> values, int i, int j)
    {
        int temp = values.get(i);
        values.set(i, values.get(j));
        values.set(j, temp);
    }


    private static int GetPivotRow(List<Integer> rows, double[][] A, int c)
    {
        double maxValue = Double.MIN_VALUE;
        double val = 0;
        int pivot_row = 0;
        for (int r : rows)
        {
            val = A[r][c];
            if (val > maxValue)
            {
                maxValue = val;
                pivot_row = r;
            }
        }
        return pivot_row;
    }

    /// <summary>
    /// Find all the rows in the row_set such that the row has 0 in its c-th column
    /// </summary>
    /// <param name="row_set">The set of row indices from which to return the selected rows</param>
    /// <param name="A">The matrix containing all rows</param>
    /// <param name="c">The targeted column index</param>
    /// <returns>The rows in the row_set such that the row has 0 in its c-th column</returns>
    private static List<Integer> GetRowsWithNonZeroAtColIndex(HashSet<Integer> row_set, double[][] A, int c)
    {
        List<Integer> nonzero_rows = new ArrayList<Integer>();
        for (int r : row_set)
        {
            if(A[r][c]!=0)
            {
                nonzero_rows.add(r);
            }
        }
        return nonzero_rows;
    }

    public static String Summary(double[][] A)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < A.length; ++i)
        {
            if (i == A.length - 1)
            {
                sb.append(Summary(A[i]));
            }
            else
            {
                sb.append(Summary(A[i]) + "\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String Summary(double[] v)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.length; ++i)
        {
            if (i != 0)
            {
                sb.append("\t");
            }
            sb.append(String.format("%.2f", v[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}

