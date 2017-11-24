package com.github.chen0040.ml.spark.utils.views;

import java.util.Date;

public class AlgorithmViewTimePoint {
	private Date x = new Date();
	private double y;
	private int index;
	
	
	public AlgorithmViewTimePoint(int index, double y){
		this.index = index;
		this.y = y;
	}
	
	public AlgorithmViewTimePoint(Date x, double y){
		this.x = x;
		this.y = y;
	}
	
	public AlgorithmViewTimePoint(){
		
	}
	
	public Date getX(){
		return x;
	}
	
	public void setX(Date x){
		this.x = (Date)x.clone();
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public double getY(){
		return y;
	}
	
	public void setY(double y){
		this.y = y;
	}
}


