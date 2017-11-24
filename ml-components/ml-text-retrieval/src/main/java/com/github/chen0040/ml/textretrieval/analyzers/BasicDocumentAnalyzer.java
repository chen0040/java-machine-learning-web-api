package com.github.chen0040.ml.textretrieval.analyzers;

import com.github.chen0040.ml.textretrieval.filters.PorterStemmer;
import com.github.chen0040.ml.textretrieval.filters.StopWordRemoval;
import com.github.chen0040.ml.textretrieval.filters.WordSegmentation;
import com.github.chen0040.ml.textretrieval.filters.LowerCase;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public class BasicDocumentAnalyzer implements DocumentAnalyzer {
    private HashMap<String, Double> attributes = new HashMap<String, Double>();
    private LowerCase lowerCaseFilter = new LowerCase();
    private PorterStemmer porterStemmer = new PorterStemmer();
    private StopWordRemoval stopWordRemoval = new StopWordRemoval();

    public LowerCase getLowerCaseFilter() {
        return lowerCaseFilter;
    }

    public void setLowerCaseFilter(LowerCase lowerCaseFilter) {
        this.lowerCaseFilter = lowerCaseFilter;
    }

    public PorterStemmer getPorterStemmer() {
        return porterStemmer;
    }

    public void setPorterStemmer(PorterStemmer porterStemmer) {
        this.porterStemmer = porterStemmer;
    }

    public StopWordRemoval getStopWordRemoval() {
        return stopWordRemoval;
    }

    public void setStopWordRemoval(StopWordRemoval stopWordRemoval) {
        this.stopWordRemoval = stopWordRemoval;
    }

    public WordSegmentation getWordSegmentation() {
        return wordSegmentation;
    }

    public void setWordSegmentation(WordSegmentation wordSegmentation) {
        this.wordSegmentation = wordSegmentation;
    }

    private WordSegmentation wordSegmentation = new WordSegmentation();

    public static final String LOWER_CASE_ON = "lowerCaseOn";
    public static final String PORTER_STEMMER_ON = "porterStemmerOn";
    public static final String STOP_WORD_REMOVAL_ON = "stopWordRemovalOn";
    public static final String WORD_SEGMENTATION_ON = "wordSegmentationOn";

    public BasicDocumentAnalyzer(){
        setAttribute(LOWER_CASE_ON, 1);
        setAttribute(PORTER_STEMMER_ON, 1);
        setAttribute(STOP_WORD_REMOVAL_ON, 1);
        setAttribute(WORD_SEGMENTATION_ON, 1);
    }

    public void setAttribute(String name, double value){
        attributes.put(name, value);
    }

    public double getAttribute(String name){
        return attributes.get(name);
    }

    public HashMap<String, Double> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, Double> attributes) {
        this.attributes = attributes;
    }

    private boolean isLowerCaseOn(){
        return getAttribute(LOWER_CASE_ON) > 0;
    }

    private boolean isPorterStemmerOn(){
        return getAttribute(PORTER_STEMMER_ON) > 0;
    }

    private boolean isStopWordRemovalOn(){
        return getAttribute(STOP_WORD_REMOVAL_ON) > 0;
    }

    private boolean isWordSegmentationOn(){
        return getAttribute(WORD_SEGMENTATION_ON) > 0;
    }


    public List<String> analyze(List<String> content){
        if(isLowerCaseOn()){
            content = lowerCaseFilter.filter(content);
        }
        if(isStopWordRemovalOn()){
            content = stopWordRemoval.filter(content);
        }
        if(isPorterStemmerOn()){
            content = porterStemmer.filter(content);
        }
        if(isWordSegmentationOn()){
            content = wordSegmentation.filter(content);
        }

        return content;

    }
}