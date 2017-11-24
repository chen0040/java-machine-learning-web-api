package com.github.chen0040.ml.textretrieval.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public class LowerCase extends AbstractTextFilter {

    @Override
    public List<String> filter(List<String> words) {
        List<String> result = new ArrayList<>();
        for(int i=0; i < words.size(); ++i){
            result.add(words.get(i).toLowerCase());
        }
        return result;
    }

    @Override
    public Object clone(){
        return new LowerCase();
    }
}
