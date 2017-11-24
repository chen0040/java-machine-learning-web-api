package com.github.chen0040.ml.spark.core.docs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 9/9/15.
 */
public class SparkMLDocument implements Serializable {

    private List<String> words = new ArrayList<>();
    private List<String> rawContents = new ArrayList<>();
    private Map<String, String> attributes = new HashMap<>();

    public void add(List<String> words){
        this.words.addAll(words);
    }

    public void add(String word){
        this.words.add(word);
    }

    public SparkMLDocument(){
        words = new ArrayList<>();
        rawContents = new ArrayList<>();
        attributes = new HashMap<String, String>();
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String name, String value){
        attributes.put(name, value);
    }

    public String getAttribute(String name){
        return attributes.get(name);
    }

    public SparkMLDocument clone(){
        SparkMLDocument clone = new SparkMLDocument();
        for(int i=0; i < words.size(); ++i){
            clone.words.add(words.get(i));
        }

        for(String attrname : attributes.keySet()){
            clone.attributes.put(attrname, attributes.get(attrname));
        }

        for(int i=0; i < rawContents.size(); ++i){
            clone.rawContents.add(rawContents.get(i));
        }
        return clone;
    }


    public List<String> getRawContents() {
        return rawContents;
    }

    public void setRawContents(List<String> contents) {
        rawContents = contents;
    }


}
