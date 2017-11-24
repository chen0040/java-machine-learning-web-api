package com.github.chen0040.ml.sdk.models;

import com.github.chen0040.ml.commons.tables.DataColumnCollection;
import com.github.chen0040.ml.commons.tables.DataRow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 25/8/15.
 */
public class Instance {
    private Map<Integer, String> data;
    private double output;
    private String label;
    private int statusCode;
    private String statusInfo;
    private DataColumnCollection columns = new DataColumnCollection();

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public Instance(DataColumnCollection columns){
        data = new HashMap<>();
        statusCode = 200;
        statusInfo = "";
        this.columns = columns;
    }

    public Map<Integer, String> getData() {
        return data;
    }

    public DataRow toDataRow(){
        DataRow row = new DataRow(columns);

        for(Integer columnIndex : data.keySet()){
            String columnName = columns.column(columnIndex).getName();
            double value = 0;
            try{
                value = Double.parseDouble(data.get(columnIndex));
            } catch (NumberFormatException ex) {
                value = 0;
            }
            row.cell(columnName, value);
        }

        row.setLabel(label);
        row.setOutputValue(output);

        return row;
    }

    public void setData(Map<Integer, String> data) {
        this.data = data;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void set(String columnName, String value){
        int index = columns.indexOf(columnName);
        data.put(index, value);
    }


}
