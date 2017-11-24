package com.github.chen0040.ml.utils;

import java.util.Random;

public class SyntheticDataSequence {
	
	private Random random;
	private double[] data;
	private int offset;
	
	public SyntheticDataSequence(long seed, int offset, double multiplier){
		this.offset = offset;
		
		random = new Random(seed);
		data = new double[60];
		for(int i=0; i < data.length; ++i){
			data[i] = random.nextDouble() * multiplier;
		}
	}
	
	public double getValue(int i){
		int index = offset + i;
		index = index % data.length;
		return data[index];
	}

}
