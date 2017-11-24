package com.github.chen0040.ml.textretrieval.filters;

import java.io.Serializable;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public interface TextFilter extends Cloneable, Serializable {
    List<String> filter(List<String> words);
}
