package com.github.chen0040.ml.spark.core.docs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/9/15.
 */
public class SparkMLDocumentBag implements Serializable {
    private List<SparkMLDocument> docs = new ArrayList<>();

    public List<SparkMLDocument> getDocs() {
        return docs;
    }

    public void setDocs(List<SparkMLDocument> docs) {
        this.docs = docs;
    }

    public int size(){
        return docs.size();
    }

    public SparkMLDocumentBag clone(){
        SparkMLDocumentBag clone = new SparkMLDocumentBag();
        for(int i=0; i < docs.size(); ++i){
            clone.docs.add(docs.get(i).clone());
        }
        return clone;
    }

    public SparkMLDocument add(List<String> words){
        SparkMLDocument doc = new SparkMLDocument();
        doc.add(words);
        docs.add(doc);
        return doc;
    }

    public void add(String word){
        if(this.isEmpty()){
            SparkMLDocument doc = new SparkMLDocument();
            doc.add(word);
            docs.add(doc);
        }else{
            this.get(this.size()-1).add(word);
        }
    }

    public boolean isEmpty(){
        return docs.isEmpty();
    }

    public SparkMLDocument get(int i){
        return docs.get(i);
    }

    public List<String> toBagOfWords(){
        List<String> result = new ArrayList<>();
        for(int i=0; i < this.size(); ++i){
            result.addAll(this.get(i).getWords());
        }
        return result;
    }

}
