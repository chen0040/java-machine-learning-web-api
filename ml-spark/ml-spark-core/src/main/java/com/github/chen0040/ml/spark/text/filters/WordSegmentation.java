package com.github.chen0040.ml.spark.text.filters;

import java.util.List;

public class WordSegmentation extends AbstractTextFilter
{
    @Override
    public Iterable<String> filter(Iterable<String> words) {
        return words;
    }

    @Override
    public List<String> filter(List<String> words){
        return words;
    }

    @Override
    public Object clone(){
        return new WordSegmentation();
    }
}