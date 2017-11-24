package com.github.chen0040.ml.spark.utils.views;


import java.util.ArrayList;
import java.util.List;

public class AlgorithmViewTimeSeries {
	private List<AlgorithmViewTimePoint> data;
	private String name;
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public AlgorithmViewTimeSeries(){
		data = new ArrayList<AlgorithmViewTimePoint>();
	}
	
	public List<AlgorithmViewTimePoint> getData(){
		return data;
	}
	
	public void setData(List<AlgorithmViewTimePoint> ts){
		data = ts;
	}
}
