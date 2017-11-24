package com.github.chen0040.ml.spark.text.analyzers;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by root on 9/10/15.
 */
public interface DocumentAnalyzer extends Serializable{
    Iterable<String> analyze(Iterable<String> rawContents);
    JavaRDD<Iterable<String>> analyze(JavaRDD<Iterable<String>> rawContents);
    JavaPairRDD<String, Iterable<String>> analyze(JavaPairRDD<String, Iterable<String>> rawContents);

    void setAttribute(String name, double value);
    double getAttribute(String name);
    HashMap<String, Double> getAttributes();
    void setAttributes(HashMap<String, Double> attributes);
}
