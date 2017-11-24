package com.github.chen0040.ml.commons.arm;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.IntelliContext;

import java.util.ArrayList;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public class ItemSet extends ArrayList<String> {
    public int support;
    public int parentSupport;

    @Override
    public Object clone() {
        ItemSet clone = new ItemSet(this);
        clone.parentSupport = parentSupport;
        clone.support = support;
        return clone;
    }

    public ItemSet(ItemSet rhs){
        for(String item : rhs){
            add(item);
        }
        support = rhs.support;
    }

    public double confidence(){
        if(parentSupport == 0){
            return 1;
        }else{
            return (double)support / parentSupport;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"items\": ");
        sb.append("[");
        for(int i=0; i < size(); ++i){
            if(i != 0) {
                sb.append(",");
            }
            sb.append(get(i));
        }
        sb.append("], ");
        sb.append("support: ");
        sb.append(support);
        sb.append(", ");
        sb.append("confidence: ");
        sb.append(confidence());
        sb.append("}");
        return sb.toString();
    }

    public ItemSet(String item){
        support = 0;
        this.add(item);
    }

    public String getLastItem(){
        return this.get(this.size()-1);
    }

    public boolean inTuple(IntelliContext mgr, IntelliTuple tuple){
        int n = tuple.tupleLength();
        for(String item : this){
            boolean found = false;

            for(int i=0; i < n; ++i){
                String tupleItem = mgr.getAttributeValueAsString(tuple, i);
                if(tupleItem != null && tupleItem.equals(item) ){
                    found = true;
                    break;
                }
            }

            if(!found){
                return false;
            }
        }

        return true;
    }
}
