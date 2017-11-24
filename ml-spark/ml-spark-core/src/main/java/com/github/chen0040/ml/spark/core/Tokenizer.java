package com.github.chen0040.ml.spark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class Tokenizer implements Serializable{
    public List<String> tokenize(String line){
        String[] tokens = line.split(",");
        List<String> result = new ArrayList<>();
        for(String token : tokens){
            result.add(token.trim());
        }
        return result;
    }
}
