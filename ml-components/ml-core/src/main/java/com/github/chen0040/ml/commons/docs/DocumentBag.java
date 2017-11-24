package com.github.chen0040.ml.commons.docs;

import java.util.ArrayList;

/**
 * Created by root on 9/9/15.
 */
public class DocumentBag extends ArrayList<BasicDocument> implements Cloneable {
    @Override
    public Object clone(){
        DocumentBag clone = new DocumentBag();
        for(int i=0; i < this.size(); ++i){
            clone.add((BasicDocument) this.get(i).clone());
        }
        return clone;
    }

}
