package com.github.chen0040.ml.spark.text.filters;

import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public abstract class AbstractTextFilter implements TextFilter {
    public abstract Iterable<String> filter(Iterable<String> words);
    public abstract List<String> filter(List<String> words);
}
