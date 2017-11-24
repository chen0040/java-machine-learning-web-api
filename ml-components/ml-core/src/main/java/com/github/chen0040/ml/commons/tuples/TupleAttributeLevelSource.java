package com.github.chen0040.ml.commons.tuples;

import com.github.chen0040.ml.commons.tables.DataColumnCollection;

import java.util.*;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class TupleAttributeLevelSource {
    private Map<Integer, List<String>> model;
    private List<String> attributeNames;
    private Map<Integer, HashSet<String>> excludeLevels;


    public TupleAttributeLevelSource(){
        model = new HashMap<Integer, List<String>>();
        excludeLevels = new HashMap<Integer, HashSet<String>>();
        attributeNames = new ArrayList<>();
    }

    public Map<Integer, List<String>> getModel(){
        return model;
    }

    public List<String> getAttributeNames(){
        return attributeNames;
    }

    public Map<Integer, HashSet<String>> getExcludeLevels(){
        return excludeLevels;
    }

    public void excludeLevelAtAttribute(String catValue, int index){
        HashSet<String> levels = null;
        if(excludeLevels.containsKey(index)){
            levels = excludeLevels.get(index);
        }else{
            levels = new HashSet<>();
            excludeLevels.put(index, levels);
        }
        levels.add(catValue);
    }

    public boolean hasAttributeNames(){
        return attributeNames != null && !attributeNames.isEmpty();
    }

    public void setAttributeName(int index, String attributeName)
    {
        while(this.attributeNames.size() < index+1){
            this.attributeNames.add("field"+this.attributeNames.size());
        }
        this.attributeNames.set(index, attributeName);
    }

    public String getAttributeName(int index){
        if(this.attributeNames == null || this.attributeNames.size() <= index){
            return String.format("field%d", index);
        }else{
            return this.attributeNames.get(index);
        }
    }

    public boolean isAttributeMultiLevel(int index){
        return model.containsKey(index);
    }

    public void setAttributeMultiLevel(int index, boolean yes){
        if(yes){
            if(!model.containsKey(index)){
                model.put(index, new ArrayList<>());
            }
        }else{
            model.remove(index);
        }
    }

    public List<TupleAttributeLevel> getLevelsAtAttribute(int index)
    {
        List<String> levels0 = model.get(index);
        List<TupleAttributeLevel> levels = new ArrayList<TupleAttributeLevel>();
        for(int i=0; i < levels0.size(); ++i){
            levels.add(new TupleAttributeLevel(getAttributeName(index), levels0.get(i), index, i));
        }
        return levels;
    }

    public TupleAttributeLevel getLevel(int index, int levelIndex){
        List<String> levels0 = model.get(index);
        return new TupleAttributeLevel(getAttributeName(index), levels0.get(levelIndex), index, levelIndex);
    }

    public int getLevelCountAtAttribute(int index){
        return model.containsKey(index) ? model.get(index).size() : 0;
    }

    public String getLevelName(int index, int catIndex){

        if(model.containsKey(index)){
            List<String> category_values= model.get(index);
            if(category_values.size() <= catIndex){ return null; }
            return category_values.get(catIndex);
        }else{
            return null;
        }
    }

    public List<String> getLevelDescriptorsAtAttribute(int index){
        String header1 = String.format("field%d", index);
        if(attributeNames != null && attributeNames.size() > index) {
            header1 = attributeNames.get(index);
        }
        List<String> formattedHeaders = new ArrayList<>();
        List<String> subheaders = model.get(index);
        int header2Count = subheaders.size();
        for(int i=0; i < header2Count; ++i){
            formattedHeaders.add(String.format("%s[%s]", header1, subheaders.get(i)));
        }
        return formattedHeaders;
    }

    private boolean shouldExclude(String catValue, int index){
        if(excludeLevels.containsKey(index)){
            HashSet<String> levels = excludeLevels.get(index);
            return levels.contains(catValue);
        }
        return false;
    }

    public int getLevelIndex(int index, String catValue){
        List<String> category_values = null;
        if(model.containsKey(index)){
            category_values= model.get(index);
        }else{
            category_values = new ArrayList<>();
            model.put(index, category_values);
        }
        int selected_index = -1;
        for(int i=0; i < category_values.size(); ++i){
            if(category_values.get(i).equals(catValue)){
                selected_index = i;
                break;
            }
        }
        if(selected_index == -1 && !shouldExclude(catValue, index)){
            selected_index = category_values.size();
            category_values.add(catValue);
        }
        return selected_index;
    }

    public DataColumnCollection columns() {
        DataColumnCollection columns = new DataColumnCollection();

        for(int i=0; i < attributeNames.size(); ++i){
            columns.add(attributeNames.get(i));
        }

        return columns;
    }
}
