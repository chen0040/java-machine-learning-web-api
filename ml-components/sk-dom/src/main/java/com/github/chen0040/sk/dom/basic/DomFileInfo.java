package com.github.chen0040.sk.dom.basic;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class DomFileInfo {
    private int maxColCountInRow;
    private int minColCountInRow;
    private int rowCount;

    public boolean isTable(){
        return maxColCountInRow != 0 && maxColCountInRow == minColCountInRow;
    }

    public int getColCount(){
        return minColCountInRow;
    }
    public int getRowCount() { return rowCount; }

    public DomFileInfo(){
        this.maxColCountInRow=0;
        this.minColCountInRow=Integer.MAX_VALUE;
        this.rowCount = 0;
    }

    public void scan(String[] values){
        maxColCountInRow = Math.max(maxColCountInRow, values.length);
        minColCountInRow = Math.min(minColCountInRow, values.length);
        rowCount++;
    }

    public void scan(DomElement line){
        scan(line.data);
    }
}
