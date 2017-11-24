package com.github.chen0040.ml.ann.kohonen;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

public class SOFMNet implements  Cloneable {
	private static Random random = new Random();
	private int rows;
	private int cols;
	private double sigma0;
	private double tau1;
	private int selfOrganizingPhaseEpoches =1000; //the iteration number in the phase 1: self-organizing phase
	private int epoches =0;
	private double eta0 =0.1;
	private int inputDimension =0;
	private ArrayList<SOFMNeuron> neurons;

	public void copy(SOFMNet rhs){
		rows = rhs.rows;
		cols = rhs.cols;
		sigma0 = rhs.sigma0;
		tau1 = rhs.tau1;
		selfOrganizingPhaseEpoches = rhs.selfOrganizingPhaseEpoches;
		epoches = rhs.epoches;
		eta0 = rhs.eta0;
		inputDimension = rhs.inputDimension;
		neurons.clear();

		for(int i=0; i < rhs.neurons.size(); ++i){
			neurons.add((SOFMNeuron)rhs.neurons.get(i).clone());
		}
	}

	@Override
	public Object clone(){
		SOFMNet clone = new SOFMNet();
		clone.copy(this);

		return clone;
	}

	public SOFMNet(){
		neurons = new ArrayList<SOFMNeuron>();
	}
	
	//input_size should be smaller or equal to the actual input size
	public SOFMNet(int rows, int cols, int inputDimension)
	{
		this.rows =rows;
		this.cols =cols;
		this.inputDimension =inputDimension;

		neurons = new ArrayList<SOFMNeuron>();
		for(int y=0; y< this.rows; ++y)
		{
			for(int x=0; x< this.cols; ++x)
			{
				SOFMNeuron neuron=new SOFMNeuron();
				neuron.x = x;
				neuron.y = y;
				neuron.weights = new double[this.inputDimension];
				neuron.output = x * this.rows + y;
				neurons.add(neuron);
			}
		}

		sigma0 = 0.707*Math.sqrt((this.rows -1)*(this.rows -1)+(this.cols -1)*(this.cols -1));
		tau1 = 1000 / Math.log(sigma0);
	}
	
	public void setEta0(double eta0)
	{
		this.eta0 =eta0;
	}
	
	public void setEpochesForSelfOrganizingPhase(int epoches)
	{
		selfOrganizingPhaseEpoches =epoches;
	}
	
	public void setSelfOrganizingPhaseEpochCount(int sop)
	{
		selfOrganizingPhaseEpoches =sop;
	}

	public SOFMNeuron neuronAt(int row, int col)
	{
		return neurons.get(row * cols + col);
	}

	public void initialize(double[] weight_lower_bounds, double[] weight_upper_bounds)
	{
		Vector<Double> w1=new Vector<Double>();
		for(int k=0; k<weight_lower_bounds.length; k++)
		{
			w1.add(new Double(weight_lower_bounds[k]));
		}
		Vector<Double> w2=new Vector<Double>();
		for(int k=0; k < weight_upper_bounds.length; k++)
		{
			w2.add(new Double(weight_upper_bounds[k]));
		}
		this.initialize(w1, w2);
	}
	
	public void initialize(Vector<Double> lowest_weight, Vector<Double> highest_weight)
	{
		int [] seq=new int[neurons.size()];
		for(int i=0; i< neurons.size(); ++i)
		{
			seq[i]=i;
		}
		
		
		for(int j=0; j< inputDimension; ++j)
		{
			double inc=(highest_weight.get(j).doubleValue() - lowest_weight.get(j).doubleValue()) / neurons.size();
			
			for(int i=0; i< neurons.size(); ++i)
			{
				int k= random.nextInt(neurons.size());
				int tmp=seq[i];
				seq[i]=seq[k];
				seq[k]=tmp;
			}
			
			for(int i=0; i< neurons.size(); ++i)
			{
				SOFMNeuron neuron= neurons.get(i);
				neuron.weights[j] = lowest_weight.get(j).doubleValue()+inc*seq[i]+inc* random.nextDouble();
			}
		}
		
		epoches =0;
	}
	
	protected double eta(int n)
	{
		double result= eta0 * Math.exp(-n/ tau1);
		if(result < 0.01) result=0.01;
		return result;
	}
	
	protected double h(double distance, int n)
	{
		if(n < selfOrganizingPhaseEpoches) //self-organizing phase
		{
			double sigma= sigma0 * Math.exp(- n / tau1);
			return Math.exp(-distance * distance / (2*sigma*sigma));
		}
		else //convergence phase
		{
			return 1;
		}
	}
	
	public void train(double[] input)
	{
		//determine the winner neuron
		SOFMNeuron m_winner=null;
		double max_sum = Double.MAX_VALUE;
		for(int i=0; i< neurons.size(); i++)
		{
			SOFMNeuron neuron= neurons.get(i);
			double sum=0;
			for(int j=0; j< inputDimension; j++)
			{
				double d=input[j] - neuron.weights[j];
				sum+=d*d;
			}
			if(sum < max_sum)
			{
				m_winner=neuron;
				max_sum=sum;
			}
		}
		
		//update neuron weights
		for(int i=0; i< neurons.size(); ++i)
		{
			SOFMNeuron neuron= neurons.get(i);
			if(neuron == m_winner) continue;
			double d=neuron.getDistance(m_winner);
			for(int j=0; j< inputDimension; ++j)
			{
				double current_weight=neuron.weights[j];
				double weight_delta=eta(epoches) * h(d, epoches) * (input[j] - current_weight);
				neuron.weights[j] = current_weight+weight_delta;
			}
		}
		
		
		epoches++;
	}
	
	public SOFMNeuron match(double[] input)
	{
		SOFMNeuron winner=null;
		double max_sum=Double.MAX_VALUE;
		for(int i=0; i< neurons.size(); i++)
		{
			SOFMNeuron neuron= neurons.get(i);
			double sum=0;
			for(int j=0; j< inputDimension; j++)
			{
				double d=input[j] - neuron.weights[j];
				sum+=d*d;
			}
			if(sum < max_sum)
			{
				winner=neuron;
				max_sum=sum;
			}
		}

		return winner;
	}
}
