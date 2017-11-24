package com.github.chen0040.ml.sdk.models.es;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by root on 9/7/15.
 */
public class ESTimeWindowDoc extends HashMap<String, Double> {
    public String label;
    private ArrayList<String> rawContents;

    public ESTimeWindowDoc(){
        rawContents = new ArrayList<>();
    }

    public void addRawContent(String line){
        rawContents.add(line);
    }

    public ArrayList<String> rawContents(){
        return rawContents;
    }
}
