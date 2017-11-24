package com.github.chen0040.ml.textretrieval.tokenizers;

import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public interface Tokenizer {
    List<String> tokenize(String text);
}
