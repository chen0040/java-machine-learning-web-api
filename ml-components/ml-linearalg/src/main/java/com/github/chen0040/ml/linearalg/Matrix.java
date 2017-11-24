package com.github.chen0040.ml.linearalg;

import com.github.chen0040.ml.linearalg.cholesky.Cholesky;
import com.github.chen0040.ml.linearalg.cholesky.CholeskyFactorization;
import com.github.chen0040.ml.linearalg.eigens.EigenVectorFactorization;
import com.github.chen0040.ml.linearalg.eigens.UT;
import com.github.chen0040.ml.linearalg.qr.QRFactorization;
import com.github.chen0040.ml.linearalg.qr.QRSolver;
import com.github.chen0040.ml.linearalg.qr.QR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class Matrix implements Serializable, Cloneable {
    private HashMap<Integer, Vector> rows = new HashMap<Integer, Vector>();
    private int rowCount;
    private int columnCount;
    private double defaultValue;

    public Matrix(){

    }

    public Matrix(double[][] A){
        for(int i = 0; i < A.length; ++i){
            double[] B = A[i];
            for(int j=0; j < B.length; ++j){
                set(i, j, B[j]);
            }
        }
    }

    public void setRow(int rowIndex, Vector rowVector){
        rowVector.setId(rowIndex);
        rows.put(rowIndex, rowVector);
    }

    public QR QR(){
        return QRFactorization.factorize(this);
    }

    public UT eigens(){
        return EigenVectorFactorization.factorize(this);
    }

    public UT eigens(int K, double epsilon){
        return EigenVectorFactorization.factorize(this, K, epsilon);
    }

    public static Matrix identity(int dimension){
        Matrix m = new Matrix(dimension, dimension);
        for(int i=0; i < m.getRowCount(); ++i){
            m.set(i, i, 1);
        }
        return m;
    }

    @Override
    public boolean equals(Object rhs){
        if(rhs != null && rhs instanceof Matrix){
            Matrix rhs2 = (Matrix)rhs;
            if(rowCount != rhs2.rowCount || columnCount != rhs2.columnCount){
                return false;
            }

            for(Integer index : rows.keySet()){
                if(!rhs2.rows.containsKey(index)) return false;
                if(!rows.get(index).equals(rhs2.rows.get(index))){
                    return false;
                }
            }

            if(defaultValue != rhs2.defaultValue){
                for(int i=0; i < rowCount; ++i){
                    if(!rows.containsKey(i)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public Object clone(){
        Matrix clone = new Matrix(rowCount, columnCount);
        clone.copy(this);
        return clone;
    }

    public void copy(Matrix rhs){
        rowCount = rhs.rowCount;
        columnCount = rhs.columnCount;
        defaultValue = rhs.defaultValue;

        rows.clear();

        for(Map.Entry<Integer, Vector> entry : rows.entrySet()){
          rows.put(entry.getKey(), (Vector)entry.getValue().clone());
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public double getDefaultValue(){
        return defaultValue;
    }

    public void setDefaultValue(double defaultValue){
        this.defaultValue = defaultValue;
    }

    public void set(int rowIndex, int columnIndex, double value){
        Vector row = getRow(rowIndex);
        row.set(columnIndex, value);
        if(rowIndex >= rowCount) { rowCount = rowIndex+1; }
        if(columnIndex >= columnCount) { columnCount = columnIndex + 1; }
    }

    public HashMap<Integer, Vector> getRows(){
        return rows;
    }

    public Matrix(int rowCount, int columnCount){
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.defaultValue = 0;
    }

    public Vector getRow(int rowIndex){
        Vector row = rows.get(rowIndex);
        if(row == null){
            row = new Vector(columnCount);
            row.setAll(defaultValue);
            row.setId(rowIndex);
            rows.put(rowIndex, row);
        }
        return row;
    }

    public void setAll(double value){
        defaultValue = value;
        for(Vector row : rows.values()){
            row.setAll(value);
        }
    }

    public double get(int rowIndex, int columnIndex) {
        Vector row=getRow(rowIndex);
        return row.get(columnIndex);
    }

    public List<Vector> columnVectors()
    {
        Matrix A = this;
        int n = A.getColumnCount();
        int rowCount = A.getRowCount();

        List<Vector> Acols = new ArrayList<Vector>();

        for (int c = 0; c < n; ++c)
        {
            Vector Acol = new Vector(rowCount);
            Acol.setAll(defaultValue);
            Acol.setId(c);

            for (int r = 0; r < rowCount; ++r)
            {
                Acol.set(r, A.get(r, c));
            }
            Acols.add(Acol);
        }
        return Acols;
    }

    public Matrix multiply(Matrix rhs)
    {
        if(this.getColumnCount() != rhs.getRowCount()){
            System.err.println("A.columnCount must be equal to B.rowCount in multiplication");
            return null;
        }

        Vector row1;
        Vector col2;

        Matrix result = new Matrix(getRowCount(), rhs.getColumnCount());
        result.setAll(defaultValue);

        List<Vector> rhsColumns = rhs.columnVectors();

        for (Map.Entry<Integer, Vector> entry : rows.entrySet())
        {
            int r1 = entry.getKey();
            row1 = entry.getValue();
            for (int c2 = 0; c2 < rhsColumns.size(); ++c2)
            {
                col2 = rhsColumns.get(c2);
                result.set(r1, c2, row1.multiply(col2));
            }
        }

        return result;
    }

    public boolean isSymmetric(){
        if (getRowCount() != getColumnCount()) return false;

        for (Map.Entry<Integer, Vector> rowEntry : rows.entrySet())
        {
            int row = rowEntry.getKey();
            Vector rowVec = rowEntry.getValue();

            for (Integer col : rowVec.getData().keySet())
            {
                if (row == col.intValue()) continue;
                if(DoubleUtils.equals(rowVec.get(col), this.get(col, row))){
                    return false;
                }
            }
        }

        return true;
    }

    public Vector multiply(Vector rhs)
    {
        if(this.getColumnCount() != rhs.getDimension()){
            System.err.println("columnCount must be equal to the size of the vector for multiplication");
        }

        Vector row1;
        Vector result = new Vector(getRowCount());
        for (Map.Entry<Integer, Vector> entry : rows.entrySet())
        {
            row1 = entry.getValue();
            result.set(entry.getKey(), row1.multiply(rhs));
        }
        return result;
    }

    public Matrix inverse(){
        return QRSolver.invert(this);
    }

    public Cholesky cholesky(){
        return CholeskyFactorization.factorize(this);
    }

    public Matrix transpose(){
        List<Vector> columns = columnVectors();
        Matrix t = new Matrix(getColumnCount(), getRowCount());
        for(int i=0; i < columns.size(); ++i){
            Vector column = columns.get(i);
            if(!column.isZero()){
                t.setRow(i, column);
            }
        }
        return t;
    }


}
