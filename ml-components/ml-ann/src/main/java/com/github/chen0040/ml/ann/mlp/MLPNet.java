package com.github.chen0040.ml.ann.mlp;

import com.github.chen0040.ml.ann.functions.TransferFunction;

import java.util.ArrayList;

//default network assumes input and output are in the range of [0, 1] 
public class MLPNet implements  Cloneable {
	protected MLPLayer inputLayer =null;
	protected MLPLayer outputLayer =null;

	protected ArrayList<MLPLayer> hiddenLayers;

	protected double learningRate =0.25; //learning rate
	protected double momentum =0.9; //momentum term for \Delta w[i][j]

	public void copy(MLPNet rhs){
		inputLayer = rhs.inputLayer == null ? null : (MLPLayer)rhs.inputLayer.clone();
		outputLayer = rhs.outputLayer == null ? null : (MLPLayer)rhs.outputLayer.clone();
		hiddenLayers.clear();

		for(int i=0; i < rhs.hiddenLayers.size(); ++i){
			hiddenLayers.add((MLPLayer)rhs.hiddenLayers.get(i).clone());
		}

		learningRate = rhs.learningRate;
		momentum = rhs.momentum;
	}

	public MLPLayer createInputLayer(int dimension){
		inputLayer = new MLPLayer(dimension);
		return inputLayer;
	}

	public MLPLayer createOutputLayer(int dimension){
		outputLayer = new MLPLayer(dimension);
		return outputLayer;
	}



	
	public MLPNet()
	{
		hiddenLayers = new ArrayList<MLPLayer>();
	}

	
	public double getLearningRate()
	{
		return learningRate;
	}
	
	public double getMomentum()
	{
		return momentum;
	}
	
	public void setMomentum(double alpha)
	{
		momentum =alpha;
	}
	
	public void setLearningRate(double eta)
	{
		learningRate =eta;
	}
	
	public void addHiddenLayer(int neuron_count)
	{
		MLPLayer layer=new MLPLayer(neuron_count);
		hiddenLayers.add(layer);
	}
	
	public void addHiddenLayer(int neuron_count, TransferFunction transfer_function)
	{
		MLPLayer layer=new MLPLayer(neuron_count);
		layer.setTransfer(transfer_function);
		hiddenLayers.add(layer);
	}
	
	public double train(double[] input, double[] target)
	{
		//forward propagate
		double[] propagated_output = inputLayer.setOutput(input);
		for(int i=0; i < hiddenLayers.size(); ++i) {
			propagated_output = hiddenLayers.get(i).forward_propagate(propagated_output);
		}
		propagated_output = outputLayer.forward_propagate(propagated_output);
		
		double error = get_target_error(target);
		
		//backward propagate
		double[] propagated_error = outputLayer.back_propagate(minus(target, propagated_output));
		for(int i = hiddenLayers.size()-1; i >= 0; --i){
			propagated_error = hiddenLayers.get(i).back_propagate(propagated_error);
		}

		//adjust weights
		double[] input2 = inputLayer.output();
		for(int i = 0; i < hiddenLayers.size(); ++i){
			hiddenLayers.get(i).adjust_weights(input2, getLearningRate(), getMomentum());
			input2 = hiddenLayers.get(i).output();
		}
		outputLayer.adjust_weights(input2, getLearningRate(), getMomentum());

		
		return error; 
	}

	public double[] minus(double[] a, double[] b){
		double[] c = new double[a.length];
		for(int i=0; i < a.length; ++i){
			c[i] = a[i] - b[i];
		}
		return c;
	}
	
	protected double get_target_error(double[] target)
	{
		double t_error=0;
		double error=0;
		double[] output = outputLayer.output();
		for(int i=0; i< output.length; i++)
		{
			error = target[i] - output[i];
			t_error+=(0.5 * error * error);
		}
		
		return t_error;
	}
	
	public double test(double[] input, double[] target)
	{
		double[] propagated_output = inputLayer.setOutput(input);
		for(int i=0; i < hiddenLayers.size(); ++i) {
			propagated_output = hiddenLayers.get(i).forward_propagate(propagated_output);
		}
		propagated_output = outputLayer.forward_propagate(propagated_output);
		
		return get_target_error(target);
	}
	
	public double[] predict(double[] input)
	{
		double[] propagated_output = inputLayer.setOutput(input);
		for(int i=0; i < hiddenLayers.size(); ++i) {
			propagated_output = hiddenLayers.get(i).forward_propagate(propagated_output);
		}
		propagated_output = outputLayer.forward_propagate(propagated_output);

		return propagated_output;
	}
	
}
