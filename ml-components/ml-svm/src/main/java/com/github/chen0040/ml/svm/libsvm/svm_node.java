package com.github.chen0040.ml.svm.libsvm;
public class svm_node implements Cloneable, java.io.Serializable
{
	public int index;
	public double value;

	public void copy(svm_node rhs){
		index = rhs.index;
		value = rhs.value;
	}

	@Override
	public Object clone(){
		svm_node clone = new svm_node();
		clone.copy(this);
		return clone;
	}
}
