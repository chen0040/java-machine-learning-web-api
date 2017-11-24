package com.github.chen0040.ml.models;

import com.github.chen0040.ml.enums.AlgorithmViewType;

public interface AlgorithmView extends Jsonable{
	AlgorithmViewType getViewType();
	
	void setViewType(AlgorithmViewType viewType);
	
	String getTitle();
	void setTitle(String title);
	
	String getId();
	void setId(String id);
	
	String getAlgorithmId();
	void setAlgorithmId(String algorithmId);
	
	long getTimeWindowInMinutes();
	void setTimeWindowInMinutes(long timeWindowInMinutes);
	
	long getEndTime();
	void setEndTime(long endTime);
	
	long getStartTime();
	void setStartTime(long startTime);

}
