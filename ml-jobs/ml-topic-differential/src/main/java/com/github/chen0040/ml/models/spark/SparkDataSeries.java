package com.github.chen0040.ml.models.spark;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by root on 11/9/15.
 */
public class SparkDataSeries implements Serializable {
    
	private static final long serialVersionUID = 1L;
	private String name;
	private int isHistogramData = 1;

    public ArrayList<SparkDataPoint> data = new ArrayList<SparkDataPoint>();
    
    public int getIsHistogramData(){
        return isHistogramData;
    }

    public void setIsHistogramData(int isHistogramData){
        this.isHistogramData = isHistogramData;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(int index, String label, double value){
        data.add(new SparkDataPoint(index, label, value));
    }
    
    public int size(){
    	return data.size();
    }
    
    public SparkDataPoint get(int index){
    	return data.get(index);
    }
    
    public ArrayList<SparkDataPoint> getData(){
    	return data;
    }
    
    public void setData(ArrayList<SparkDataPoint> data){
    	this.data = data;
    }
    
    public void sortDescendingly() {
        data.sort((s1, s2)->
        {
            if(s1.value > s2.value) return -1;
            else if(s1.value < s2.value) return 1;
            else return 0;
        });
        for(int i=0; i < data.size(); ++i){
            data.get(i).index = i;
        }
    }

    public void sortAscendingly(){
        data.sort((s1, s2)->{
            if(s1.value > s2.value) return 1;
            else if(s1.value < s2.value) return -1;
            else return 0;
        });
        for(int i=0; i < data.size(); ++i){
            data.get(i).index = i;
        }
    }
}
