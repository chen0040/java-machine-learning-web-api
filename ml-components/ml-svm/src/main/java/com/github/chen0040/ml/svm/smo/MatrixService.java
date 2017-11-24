package com.github.chen0040.ml.svm.smo;

import Jama.Matrix;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class MatrixService {

    // return the covariance matrix of X, which consists a set of records, each of which represented by double[]
    public static Matrix cov(List<double[]> X) {
        Matrix muVector = mean(X);
        return cov(X, muVector);
    }

    public static Matrix cov(List<double[]> X, Matrix muVector){
        int dimension = X.get(0).length;



        Matrix covarianceMatrix = new Matrix(dimension, dimension);

        for(int i=0; i < dimension; ++i){
            for(int j=0; j < dimension; ++j){
                covarianceMatrix.set(i, j, computeCovariance(X, muVector, i, j));
            }
        }
        return covarianceMatrix;
    }

    // returns a column vector, in which a single value in each row is the mean of a column in X
    public static Matrix mean(List<double[]> X){
        int dimension = X.get(0).length;
        Matrix muVector = new Matrix(dimension, 1);

        int m = X.size();
        for(int k=0; k < dimension; ++k){
            double sum = 0;
            for(int i=0; i < m; ++i){
                sum += X.get(m)[k];
            }
            muVector.set(k, 0, sum / m);
        }

        return muVector;
    }

    private static double computeCovariance(List<double[]> X, Matrix MU, int j, int k){
        int N = X.size();
        double sum = 0;
        for(int i=0; i < N; ++i) {
            sum += (X.get(i)[j] - MU.get(j, 0)) * (X.get(i)[k] - MU.get(k, 0));
        }
        return sum / (1-N);
    }

    // return a matrix, whose row is a double[] record in X
    public static Matrix toMatrix(List<double[]> X){
        int m = X.size();
        int n = X.get(0).length;
        Matrix XX = new Matrix(m, n);
        for(int i=0; i < m; ++i){
            for(int j=0; j < n; ++j){
                XX.set(i, j, X.get(i)[j]);
            }
        }

        return XX;
    }

    public static Matrix zeros(int row, int col){
        return new Matrix(row, col);

    }

    public static Matrix zeros(int m){
        return zeros(m, m);
    }

    public static Matrix sum(Matrix matrix){
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();
        Matrix rowVec = new Matrix(1, cols);
        for(int j=0; j < cols; ++j){
            double sum = 0;
            for(int i=0; i < rows; ++i){
                sum += matrix.get(i, j);
            }
            rowVec.set(0, j, sum);
        }
        return rowVec;
    }

    public static Matrix sum(Matrix matrix, int dimension){
        if(dimension==2) {
            int rows = matrix.getRowDimension();
            int cols = matrix.getColumnDimension();
            Matrix columnVec = new Matrix(rows, 1);
            for (int i = 0; i < rows; ++i) {
                double sum = 0;
                for (int j = 0; j < cols; ++j) {
                    sum += matrix.get(i, j);
                }
                columnVec.set(i, 0, sum);
            }
            return columnVec;
        }
        else{
            return sum(matrix);
        }

    }

    public static double vecSum(Matrix vector){
        int rows = vector.getRowDimension();
        int cols = vector.getColumnDimension();
        double sum = 0;
        for(int j=0; j < cols; ++j){
            for(int i=0; i < rows; ++i){
                sum += vector.get(i, j);
            }
        }
        return sum;
    }

    public static Matrix column(Matrix matrix, int colIndex){
        int rows = matrix.getRowDimension();
        Matrix column = new Matrix(rows, 1);
        for(int i=0; i< rows; ++i){
            column.set(i, 0, matrix.get(i, colIndex));
        }
        return column;
    }

    public static Matrix row(Matrix matrix, int rowIndex){
        int cols = matrix.getColumnDimension();
        Matrix row = new Matrix(1, cols);
        for(int i=0; i < cols; ++i){
            row.set(0, i, matrix.get(rowIndex, i));
        }
        return row;
    }

    public static Matrix pow(Matrix matrix, double power){
        int rows = matrix.getRowDimension();
        int cols = matrix.getColumnDimension();

        Matrix result = new Matrix(rows, cols);
        for(int i=0; i < rows; ++i){
            for(int j=0; j < cols; ++j){
                result.set(i, j, Math.pow(matrix.get(i, j), power));
            }
        }

        return result;
    }

    private static Random random = new Random();

    public static double rand(){
        return random.nextDouble();
    }

    public static Matrix bsxfun(BiFunction<Double, Double, Double> operation, Matrix A, Matrix B){
        int rowCountA = A.getRowDimension();
        int colCountA = A.getColumnDimension();
        int rowCountB = B.getRowDimension();
        int colCountB = B.getColumnDimension();

        int rowCountC = Math.max(rowCountA, rowCountB);
        int colCountC = Math.max(colCountA, colCountB);

        Matrix C = new Matrix(rowCountC,  colCountC);

        for(int i=0; i < rowCountC; ++i){
            int rowIndexA = i < rowCountA ? i : rowCountA - 1;
            int rowIndexB = i < rowCountB ? i : rowCountB - 1;
            for(int j=0; j < colCountC; ++j){
                int colIndexA = j < colCountA ? j : colCountA - 1;
                int colIndexB = j < colCountB ? j : colCountB - 1;
                double cellA = A.get(rowIndexA, colIndexA);
                double cellB = B.get(rowIndexB, colIndexB);
                C.set(i, j, operation.apply(cellA, cellB));
            }
        }

        return C;
    }

    public static Matrix bsxfun_plus(Matrix A, Matrix B){
        return bsxfun(new BiFunction<Double, Double, Double>() {
            public Double apply(Double a, Double b) {
                return a + b;
            }
        }, A, B);
    }

    public static Matrix bsxfun_plus(Matrix A, double b){
        Matrix B = new Matrix(1, 1);
        B.set(0, 0, b);
        return bsxfun_plus(A, B);
    }

    public static Matrix bsxfun_times(Matrix A, Matrix B){
        return bsxfun(new BiFunction<Double, Double, Double>() {
            public Double apply(Double a, Double b) {
                return a * b;
            }
        }, A, B);
    }

    public static Matrix pow(double val, Matrix P){
        int rowCount = P.getRowDimension();
        int colCount = P.getColumnDimension();
        Matrix C = new Matrix(rowCount, colCount);

        for(int i=0; i < rowCount; ++i){
            for(int j=0; j < colCount; ++j){
                C.set(i, j, Math.pow(val, P.get(i, j)));
            }
        }

        return C;

    }

    public static Matrix filterRows(Matrix matrix, List<Integer> selectedRowIndices){
        int colCount = matrix.getColumnDimension();
        int rowCount = selectedRowIndices.size();
        Matrix result = new Matrix(rowCount, colCount);
        for(int i = 0; i < rowCount; ++i){
            int rowIndex = selectedRowIndices.get(i);
            for(int j=0; j < colCount; ++j){
                result.set(i, j, matrix.get(rowIndex, j));
            }
        }
        return result;
    }

    public static Matrix plus(Matrix A, double b){
        int colCount = A.getColumnDimension();
        int rowCount = A.getRowDimension();

        Matrix C = new Matrix(rowCount, colCount);

        for(int i=0; i < rowCount; ++i){
            for(int j=0; j < colCount; ++j){
                C.set(i, j, A.get(i, j)+b);
            }
        }

        return C;
    }

}
