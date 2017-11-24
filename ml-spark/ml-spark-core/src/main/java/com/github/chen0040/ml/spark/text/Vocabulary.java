package com.github.chen0040.ml.spark.text;

import java.io.Serializable;
import java.util.List;

/**
 * Created by memeanalytics on 14/8/15.
 */
public interface Vocabulary extends Cloneable, Serializable {
    String get(int index);
    int getLength();
    void add(String word);
    boolean contains(String word);
    void setWords(List<String> words);

    int indexOf(String word);
}
