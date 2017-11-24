package com.github.chen0040.ml.spark.text.filters;

import java.io.Serializable;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public interface TextFilter extends Serializable, Cloneable {
    Iterable<String> filter(Iterable<String> words);
    List<String> filter(List<String> words);
}
