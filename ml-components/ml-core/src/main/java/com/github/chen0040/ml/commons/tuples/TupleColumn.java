package com.github.chen0040.ml.commons.tuples;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class TupleColumn {

    protected int origColumnIndex;
    protected String header;

    public TupleColumn(String header){
        origColumnIndex = -1;
        this.header = header;
    }

    public TupleColumn(String header, int origColumnIndex){
        this.origColumnIndex = origColumnIndex;
        this.header = header;
    }

    public TupleColumn(int origColumnIndex){
        this.origColumnIndex = origColumnIndex;
    }

    public void setOrigColumnIndex(int index){
        origColumnIndex=index;
        if(this.header==null){
            this.header = String.format("field%d", origColumnIndex);
        }
    }

    public int getOrigColumnIndex(){
        return origColumnIndex;
    }

    @Override
    public int hashCode(){
        return origColumnIndex;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof  TupleColumn){
            TupleColumn cast_obj = (TupleColumn)obj;
            return cast_obj.getOrigColumnIndex() == origColumnIndex;
        }
        return false;
    }

    @Override
    public String toString(){
        return this.header;
    }


    public String getHeader(){
        return header;
    }
}
