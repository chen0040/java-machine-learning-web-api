package com.github.chen0040.ml.textretrieval.filters;

import java.util.List;

public class WordSegmentation extends AbstractTextFilter
{
    @Override
    public List<String> filter(List<String> words) {
        return words;
    }

    @Override
    public Object clone(){
        return new WordSegmentation();
    }
}