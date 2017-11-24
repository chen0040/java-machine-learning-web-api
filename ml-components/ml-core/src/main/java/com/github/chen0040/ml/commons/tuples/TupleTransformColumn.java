package com.github.chen0040.ml.commons.tuples;

import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class TupleTransformColumn extends TupleColumn {
    private BiFunction<Integer, Double, Double> transform;

    public TupleTransformColumn(String header, BiFunction<Integer, Double, Double> transform){
        super(header);
        this.transform = transform;
    }

    public Double apply(int index, double value){
        return transform.apply(index, value);
    }


    @Override
    public int hashCode(){
        int big_prime = 2147483647; //2ˆ31 - 1
        int hash = (super.hashCode() * 37 + header.hashCode()) % big_prime;
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof  TupleColumn){
            TupleColumn cast_obj = (TupleColumn)obj;
            if(cast_obj.getOrigColumnIndex() == origColumnIndex){
                return this.header.equals(cast_obj.header);
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return this.header;
    }

}
