package com.github.chen0040.ml.ann.kohonen;

public class SOFMNeuron implements Cloneable {
	public int x;
	public int y;
	public int output;
	public double[] weights;
	
	public SOFMNeuron()
	{

	}
	
	public double getDistance(SOFMNeuron rhs)
	{
		double dx=rhs.x - x;
		double dy=rhs.y - y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	public Object clone(){
		SOFMNeuron clone = new SOFMNeuron();
		clone.copy(this);

		return clone;
	}

	public void copy(SOFMNeuron rhs){
		x = rhs.x;
		y = rhs.y;
		output = rhs.output;
		weights = rhs.weights==null ? null : rhs.weights.clone();
	}
}
