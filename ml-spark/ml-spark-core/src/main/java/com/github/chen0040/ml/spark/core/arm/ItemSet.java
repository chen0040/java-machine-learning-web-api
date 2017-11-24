package com.github.chen0040.ml.spark.core.arm;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public class ItemSet extends ArrayList<String> implements Serializable {
    public int support;
    public int parentSupport;

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

    @Override
    public Object clone() {
        ItemSet clone = new ItemSet(this);
        clone.support = support;
        clone.parentSupport = parentSupport;
        return clone;
    }

    public ItemSet(ItemSet rhs){
        for(String item : rhs){
            add(item);
        }
        support = rhs.support;
    }

    public ItemSet(String item){
        support = 0;
        this.add(item);
    }

    public String getLastItem(){
        return this.get(this.size()-1);
    }

    public boolean isInTransaction(Iterable<String> transaction){

        for(String item : this){
            boolean found = false;

            for(String item2 : transaction){
                if(item2 != null && item2.equals(item) ){
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
