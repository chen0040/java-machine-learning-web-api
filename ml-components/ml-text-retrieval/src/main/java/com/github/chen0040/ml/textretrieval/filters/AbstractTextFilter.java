package com.github.chen0040.ml.textretrieval.filters;

import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public abstract class AbstractTextFilter implements TextFilter {
    public abstract List<String> filter(List<String> words);
}
