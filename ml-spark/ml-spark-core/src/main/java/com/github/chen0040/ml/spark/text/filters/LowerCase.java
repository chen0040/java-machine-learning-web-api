package com.github.chen0040.ml.spark.text.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public class LowerCase extends AbstractTextFilter {

    @Override
    public Iterable<String> filter(Iterable<String> words) {
        List<String> result = new ArrayList<>();
        for(String word : words) {
            result.add(word.toLowerCase());
        }
        return result;
    }

    public List<String> filter(List<String> words){
        List<String> result = new ArrayList<>();
        for(String word: words){
            result.add(word.toLowerCase());
        }
        return result;
    }

    @Override
    public Object clone(){
        return new LowerCase();
    }
}
