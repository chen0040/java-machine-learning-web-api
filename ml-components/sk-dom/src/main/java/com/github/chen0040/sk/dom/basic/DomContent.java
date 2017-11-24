package com.github.chen0040.sk.dom.basic;

import com.github.chen0040.sk.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class DomContent extends DomFileInfo {

    private List<List<String>> data;

    public DomContent(){
        super();
        data = new ArrayList<List<String>>();
    }

    public List<List<String>> toTableOfString(){
        return data;
    }

    public List<List<Double>> toTableOfDouble(){
        List<List<Double>> table = new ArrayList<List<Double>>();

        for(int i=0; i < getRowCount(); ++i){
            table.add(getRowAsDouble(i));
        }

        return table;
    }

    public List<String> getRowAsString(int i){
        return data.get(i);
    }

    public List<Integer> getRowAsInteger(int i){
        List<Integer> newRow = new ArrayList<Integer>();
        List<String> row = getRowAsString(i);
        for(int j=0; j < row.size(); ++j){
            newRow.add(StringHelper.parseInteger(row.get(j), 0));
        }
        return newRow;
    }

    public List<Double> getRowAsDouble(int i){
        List<Double> newRow = new ArrayList<Double>();
        List<String> row = getRowAsString(i);
        for(int j=0; j < row.size(); ++j){
            newRow.add(StringHelper.parseDouble(row.get(j), 0));
        }
        return newRow;
    }


    @Override
    public void scan(DomElement element){
        super.scan(element);
        String[] values = element.data;
        List<String> line = new ArrayList<>();
        for(int i=0; i < values.length; ++i){
            line.add(values[i]);
        }
        data.add(line);
    }

    public void toIntegerList(List<Integer> list){
        int rowCount = data.size();
        for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
            List<String> values = data.get(rowIndex);
            for(int colIndex = 0; colIndex < values.size(); ++colIndex) {
                list.add(StringHelper.parseInteger(values.get(colIndex), 0));
            }
        }
    }

    public void toDoubleList(List<Double> p){
        for (int i = 0; i < data.size(); ++i) {
            List<String> list = data.get(i);
            for (int j = 0; j < list.size(); ++j) {
                p.add(StringHelper.parseDouble(list.get(j), 0));
            }
        }
    }
}
