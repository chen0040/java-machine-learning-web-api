package com.github.chen0040.ml.models.spark;

public class SparkResult {
	public boolean success = true;
	public SparkResultErrorSource errorSource = SparkResultErrorSource.NA;
	public String errorMessage = "";
	public String action = "";
	public Object content = null;
}
