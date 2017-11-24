package com.github.chen0040.ml.textretrieval.analyzers;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public interface DocumentAnalyzer {
    List<String> analyze(List<String> rawContents);

    void setAttribute(String name, double value);
    double getAttribute(String name);
    HashMap<String, Double> getAttributes();
    void setAttributes(HashMap<String, Double> attributes);
}
