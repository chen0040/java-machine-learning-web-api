package com.github.chen0040.ml.commons.tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class TupleTransformRules {
    HashSet<Integer> ignoreColumns;
    HashSet<TupleColumn> addColumns;
    HashMap<Integer, List<TupleTransformColumn>> transformedColumns;

    public void setOutputColumnNumeric(int outputColumn) {
        this.outputColumn = outputColumn;
    }

    public void setOutputColumnLabel(int labelColumn) {
        this.labelColumn = labelColumn;
    }

    int outputColumn;
    int labelColumn;

    public int getOutputColumnNumeric() {
        return outputColumn;
    }

    public int getOutputColumnLabel() {
        return labelColumn;
    }



    public TupleTransformRules(){
        ignoreColumns = new HashSet<Integer>();
        transformedColumns = new HashMap<Integer, List<TupleTransformColumn>>();
        labelColumn = -1;
        outputColumn = -1;
    }

    public TupleTransformRules(int labelColumn, int outputColumn){
        ignoreColumns = new HashSet<Integer>();
        this.labelColumn = labelColumn;
        this.outputColumn = outputColumn;
    }


    public void ignoreColumn(int columnIndex){
        ignoreColumns.add(columnIndex);
    }

    public void addColumn(int columnIndex){
        if(addColumns==null){
            addColumns = new HashSet<TupleColumn>();
        }
        addColumns.add(new TupleColumn(columnIndex));
    }

    public void addColumn(TupleColumn column){
        if(column instanceof TupleTransformColumn){
            addTransformedColumn(column.getOrigColumnIndex(), (TupleTransformColumn)column);
        }else{
            if(addColumns==null){
                addColumns = new HashSet<TupleColumn>();
            }
            addColumns.add(column);
        }
    }

    private boolean shouldIgnoreColumn(int columnIndex){
        return ignoreColumns.contains(columnIndex);
    }

    public boolean shouldAddColumn(int columnIndex){
        if(columnIndex==labelColumn || columnIndex==outputColumn) return true;
        if(shouldIgnoreColumn(columnIndex)) return false;
        if(addColumns==null)
        {
            return true;
        }
        return addColumns.contains(new TupleColumn(columnIndex));
    }

    public List<TupleTransformColumn> transformsAtIndex(int index){
        if(transformedColumns.containsKey(index)){
            return transformedColumns.get(index);
        }
        return null;
    }

    public void addTransformedColumn(int index, TupleTransformColumn column){
        List<TupleTransformColumn> columns = null;
        if(transformedColumns.containsKey(index)){
            columns = transformedColumns.get(index);
        }else{
            columns = new ArrayList<TupleTransformColumn>();
            transformedColumns.put(index, columns);
        }
        column.setOrigColumnIndex(index);

        columns.add(column);
    }
}
