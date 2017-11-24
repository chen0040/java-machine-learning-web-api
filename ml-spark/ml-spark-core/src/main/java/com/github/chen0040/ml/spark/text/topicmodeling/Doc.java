package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.SparkMLTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/3/15.
 */
public class Doc {
    public int docIndex;
    public List<Token> tokens = new ArrayList<>();
    public int[] topicCounts;
    public SparkMLTuple content;
    public long timestamp;

    public Doc(int topicCount){
        topicCounts = new int[topicCount];
    }

    public void addToken(int wordIndex, int topicIndex){
        topicCounts[topicIndex]++;
        tokens.add(new Token(wordIndex, topicIndex));
    }

}
