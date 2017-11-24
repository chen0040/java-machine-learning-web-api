package com.github.chen0040.ml.textmining.topicmodeling;

import com.github.chen0040.ml.commons.IntelliTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/3/15.
 */
public class Doc {
    public int docIndex;
    public List<Token> tokens = new ArrayList<>();
    public int[] topicCounts;
    public IntelliTuple content;
    public long timestamp;

    public Doc(int topicCount){
        topicCounts = new int[topicCount];
    }

    public void addToken(int wordIndex, int topicIndex){
        topicCounts[topicIndex]++;
        tokens.add(new Token(wordIndex, topicIndex));
    }

}
