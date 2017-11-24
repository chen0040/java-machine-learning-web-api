package com.github.chen0040.ml.spark.utils.es;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 9/7/15.
 */
public class ESTimeWindowDoc implements Serializable {
    public String label;
    private List<String> rawContents = new ArrayList<>();
    private Map<String, Double> values = new HashMap<>();

    public ESTimeWindowDoc(){
        rawContents = new ArrayList<>();
    }

    public void addRawContent(String line){
        rawContents.add(line);
    }

    public List<String> rawContents(){
        return rawContents;
    }

    public ESTimeWindowKey timeWindow;

    public long startTime(){
        return timeWindow == null ? 0 : timeWindow.getStartTime();
    }

    public double get(String s) {
        return values.get(s);
    }

    public boolean containsKey(String keyword) {
        return values.containsKey(keyword);
    }

    public void put(String keyword, double v) {
        values.put(keyword, v);
    }
}
