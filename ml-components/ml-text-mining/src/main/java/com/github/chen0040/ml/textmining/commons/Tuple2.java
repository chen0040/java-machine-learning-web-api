package com.github.chen0040.ml.textmining.commons;

/**
 * Created by root on 11/5/15.
 */
public class Tuple2 <K, V> {
    private K _1;
    private V _2;

    public Tuple2(K _1, V _2){
        this._1 = _1;
        this._2 = _2;
    }

    public K _1(){
        return _1;
    }

    public V _2(){
        return _2;
    }
}
