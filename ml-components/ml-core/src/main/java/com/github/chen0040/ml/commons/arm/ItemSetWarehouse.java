package com.github.chen0040.ml.commons.arm;

import java.util.ArrayList;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class ItemSetWarehouse extends ArrayList<ItemSetCluster> {
    @Override
    public Object clone(){
        ItemSetWarehouse clone = new ItemSetWarehouse();
        clone.copy(this);
        return clone;
    }

    public void copy(ItemSetWarehouse rhs){
        clear();
        for(int i=0; i < rhs.size(); ++i){
            this.add((ItemSetCluster)rhs.get(i).clone());
        }
    }
}
