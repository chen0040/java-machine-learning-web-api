package com.github.chen0040.ml.spark.core.arm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class ItemSetCluster extends ArrayList<ItemSet> implements Serializable{
    private int setSize;

    public ItemSetCluster(int setSize){
        this.setSize = setSize;
    }

    @Override
    public Object clone(){
        ItemSetCluster clone = new ItemSetCluster(this.setSize);
        clone.copy(this);
        return clone;
    }

    public void copy(ItemSetCluster rhs){
        clear();
        this.setSize = rhs.setSize;
        for(int i=0; i < rhs.size(); ++i){
            this.add((ItemSet)rhs.get(i).clone());
        }
    }



}
