package com.github.chen0040.ml.textmining.commons.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 10/27/15.
 */
public class ARFF {
    private List<String> headers = new ArrayList<>();
    private HashMap<String, List<Integer>> attributes = new HashMap<>();
    private List<String> attributeNames = new ArrayList<>();
    private List<List<Integer>> data = new ArrayList<>();

    private String relation;

    public String getRelation() {
        return relation;
    }

    public int getAttributeCount(){
        return attributeNames.size();
    }

    public String getAttributeName(int index){
        return attributeNames.get(index);
    }

    public int get(int rowIndex, int colIndex){
        return data.get(rowIndex).get(colIndex);
    }

    public int get(int rowIndex, String attributeName){
        int colIndex = attributeNames.indexOf(attributeName);
        return get(rowIndex, colIndex);
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void addValue2Attribute(int attributeValue, String attributeName){
        List<Integer> attributeValues = null;
        if(attributes.containsKey(attributeName)) {
            attributeValues = attributes.get(attributeName);
        } else {
            attributeValues = new ArrayList<>();
            attributeNames.add(attributeName);
            attributes.put(attributeName, attributeValues);
        }
        attributeValues.add(attributeValue);
    }

    private String toString(List<Integer> list){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < list.size(); ++i){
            if(i != 0){
                sb.append(", ");
            }
            sb.append(""+list.get(i));
        }
        return sb.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("@relation "+relation);
        sb.append("\n");

        for(int i=0; i < attributeNames.size(); ++i){
            sb.append("\n");

            String attributeName = attributeNames.get(i);

            sb.append("@attribute "+attributeName+" {"+toString(attributes.get(attributeName))+"}");
        }

        sb.append("\n");
        sb.append("\n");
        sb.append("@data");
        for(int i=0; i < data.size(); ++i){
            sb.append("\n");
            List<Integer> line = data.get(i);
            sb.append(toString(line));
        }

        return sb.toString();
    }

    public int getLineCount(){
        return data.size();
    }

    public void addValue2Line(int value, int lineIndex){
        while(data.size() <= lineIndex){
            data.add(new ArrayList<>());
        }

        List<Integer> line = data.get(lineIndex);
        line.add(value);
    }

}
