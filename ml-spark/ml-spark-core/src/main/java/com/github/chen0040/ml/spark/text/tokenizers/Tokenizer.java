package com.github.chen0040.ml.spark.text.tokenizers;

/**
 * Created by root on 9/15/15.
 */
public interface Tokenizer {
    Iterable<String> tokenize(String sentence);
}
