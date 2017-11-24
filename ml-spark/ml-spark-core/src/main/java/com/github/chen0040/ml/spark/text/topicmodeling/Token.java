package com.github.chen0040.ml.spark.text.topicmodeling;

/**
 * Created by root on 11/3/15.
 */
public class Token {
    public int wordIndex;
    public int topicIndex;
    public int prevTopicIndex;

    public Token(int wordIndex, int topicIndex){
        this.wordIndex = wordIndex;
        this.topicIndex = topicIndex;
        this.prevTopicIndex = -1;
    }

    public Token(){

    }
}
