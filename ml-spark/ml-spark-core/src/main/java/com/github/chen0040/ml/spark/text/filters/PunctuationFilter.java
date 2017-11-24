package com.github.chen0040.ml.spark.text.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/5/15.
 */
public class PunctuationFilter extends AbstractTextFilter
{
    private String filter="-{}[]";
    private List<String> stripFilter = new ArrayList<>();

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<String> getStripFilter() {
        return stripFilter;
    }

    public void setStripFilter(List<String> stripFilter) {
        this.stripFilter = stripFilter;
    }

    public PunctuationFilter(){
        stripFilter.add("\\{");
        stripFilter.add("\\}");
        stripFilter.add("\\[");
        stripFilter.add("\\]");
        stripFilter.add("\\(");
        stripFilter.add("\\)");
    }

    @Override
    public List<String> filter(List<String> words) {
        List<String> result = new ArrayList<>();
        for(String word : words){
            if(!isPunctuation(word)){
                result.add(strip(word));
            }
        }
        return result;
    }

    @Override
    public Iterable<String> filter(Iterable<String> words) {
        List<String> result = new ArrayList<>();
        for(String word : words){
            if(!isPunctuation(word)){
                result.add(strip(word));
            }
        }
        return result;
    }

    private boolean isPunctuation(String w){
        w = w.trim();
        if(w.length() > 1) return false;

        return filter.contains(w);
    }

    private String strip(String word){
        for(int i=0; i < stripFilter.size(); ++i){
            word = word.replaceAll(stripFilter.get(i), "");
        }
        return word;
    }

    @Override
    public Object clone(){
        PunctuationFilter clone = new PunctuationFilter();
        clone.copy(this);
        return clone;
    }

    public void copy(PunctuationFilter rhs){
        stripFilter.clear();
        stripFilter.addAll(rhs.stripFilter);

        filter = rhs.filter;
    }
}
