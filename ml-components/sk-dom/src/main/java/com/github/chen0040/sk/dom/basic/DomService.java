package com.github.chen0040.sk.dom.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by memeanalytics on 22/8/15.
 */
public abstract class DomService {

    public abstract boolean readDoc(File file, String cvsSplitBy, final boolean hasHeader, Function<DomElement, Boolean> onLineReady, Consumer<Exception> onFailed);

    public boolean readDoc(File file, String cvsSplitBy, final boolean hasHeader, Consumer<DomContent> onCompleted, Consumer<Exception> onFailed){

        final DomContent content = new DomContent();

        boolean success = readDoc(file, cvsSplitBy, hasHeader, new Function<DomElement, Boolean>() {
            public Boolean apply(DomElement csvline) {
                content.scan(csvline);
                return true;
            }
        }, onFailed);

        if(onCompleted != null){
            onCompleted.accept(content);
        }

        return success;
    }

    public List<Integer> readIntegers(File file, String split, boolean hasHeader) {
        final List<Integer> list = new ArrayList<Integer>();
        readDoc(file, split, hasHeader, new Consumer<DomContent>() {
            public void accept(DomContent content) {
                content.toIntegerList(list);
            }
        }, null);
        return list;
    }

    public List<Double> readDoubles(File file, String split, boolean hasHeader){
        final List<Double> p = new ArrayList<Double>();
        readDoc(file, split, hasHeader, new Consumer<DomContent>() {
            public void accept(DomContent content) {
                content.toDoubleList(p);
            }
        }, null);

        return p;
    }

    public List<String> readStrings(File file, String split, boolean hasHeader){
        final List<String> p = new ArrayList<>();
        boolean success = readDoc(file, split, hasHeader, new Function<DomElement, Boolean>() {
            public Boolean apply(DomElement line) {
                String[] values = line.data;
                for (int i = 0; i < values.length; ++i) {
                    p.add(values[i]);
                }
                return true;
            }
        }, null);

        return p;
    }



    public DomFileInfo getFileInfo(File file, String csvSplitBy, boolean hasHeader, Consumer<Exception> onFailed){
        final DomFileInfo fileInfo = new DomFileInfo();

        boolean success = readDoc(file, csvSplitBy, hasHeader, new Function<DomElement, Boolean>() {
            public Boolean apply(DomElement values) {
                fileInfo.scan(values);
                return true;
            }
        }, onFailed);

        if(!success) return null;

        return fileInfo;
    }

    public DomContent readDoc(File file){
        return readDoc(file, ",", false);
    }



    public DomContent readDoc(File file, String cvsSplitBy, boolean hasHeader){
        final DomContent content = new DomContent();
        boolean success = readDoc(file, cvsSplitBy, hasHeader, new Function<DomElement, Boolean>() {
            public Boolean apply(DomElement values) {
                content.scan(values);
                return true;
            }
        }, null);

        return content;
    }

    public static String stripQuote(String sentence){
        if(sentence.startsWith("\"") && sentence.endsWith("\"")){
            return sentence.substring(1, sentence.length()-1);
        }
        return sentence;
    }

}
