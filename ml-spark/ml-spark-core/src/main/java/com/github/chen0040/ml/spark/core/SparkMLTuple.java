package com.github.chen0040.ml.spark.core;

import com.github.chen0040.ml.spark.core.docs.SparkMLDocumentBag;

import java.io.Serializable;
import java.util.*;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class SparkMLTuple implements Serializable {
    private SparkMLDocumentBag docBag = new SparkMLDocumentBag();
    private String labelOutput;
    private String predictedLabelOutput;
    private double numericOutput;
    private double predictedNumericOutput;
    private int dimension = 0;
    private String id;
    private long timestamp;
    private Map<Integer, Double> values = new TreeMap<>();

    public SparkMLDocumentBag getDocBag() {
        return docBag;
    }

    public void setDocBag(SparkMLDocumentBag docBag) {
        this.docBag = docBag;
    }

    public Map<Integer, Double> getValues() {
        return values;
    }

    public void setValues(Map<Integer, Double> values) {
        this.values = values;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    private List<String> children = new ArrayList<>();

    public Double put(Integer key, Double value) {
        Double stored = values.put(key, value);

        if(key+1 > dimension){
            dimension = key+1;
        }

        return stored;
    }

    public int size(){
        return values.size();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabelOutput() {
        return labelOutput;
    }

    public void setLabelOutput(String labelOutput) {
        this.labelOutput = labelOutput;
    }

    public String getPredictedLabelOutput() {
        return predictedLabelOutput;
    }

    public void setPredictedLabelOutput(String predictedLabelOutput) {
        this.predictedLabelOutput = predictedLabelOutput;
    }

    public double getNumericOutput() {
        return numericOutput;
    }

    public void setNumericOutput(double numericOutput) {
        this.numericOutput = numericOutput;
    }

    public double getPredictedNumericOutput() {
        return predictedNumericOutput;
    }

    public void setPredictedNumericOutput(double predictedNumericOutput) {
        this.predictedNumericOutput = predictedNumericOutput;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> toBagOfWords(){
        List<String> words = docBag.toBagOfWords();
        return words;
    }

    public void add(String word){
        docBag.add(word);
    }

    public SparkMLTuple clone(){
        SparkMLTuple clone = new SparkMLTuple();
        for(Integer i : values.keySet()){
            clone.put(i, values.get(i));
        }
        clone.labelOutput = labelOutput;
        clone.predictedLabelOutput = predictedLabelOutput;
        clone.numericOutput = numericOutput;
        clone.predictedNumericOutput = predictedNumericOutput;
        clone.dimension = dimension;
        clone.id = id;
        clone.docBag = docBag.clone();
        return clone;
    }


    public double getOrDefault(int j, double v) {
        return values.getOrDefault(j, v);
    }

    public boolean containsKey(int i) {
        return values.containsKey(i);
    }

    public double get(int i) {
        return values.get(i);
    }

    public Set<Integer> keySet() {
        return values.keySet();
    }
}
