package com.github.chen0040.sk.dom.basic;

import java.util.List;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class DomElement {
    public String[] data;
    public int lineIndex;

    public DomElement(String[] data, int lineIndex){
        this.data = data;
        this.lineIndex = lineIndex;
    }

    public DomElement(List<String> data, int lineIndex){
        String[] data2 = new String[data.size()];
        for(int i = 0; i < data.size(); ++i){
            data2[i] = data.get(i);
        }
        this.data = data2;
    }
}
