package com.github.chen0040.ml.models;

import com.github.chen0040.ml.enums.AlgorithmStatus;
import com.github.chen0040.ml.enums.AlgorithmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface AlgorithmUnit {
	String attribute(String attributeName);
	
	AlgorithmType getAlgorithmType();
	
	Map<String, String> getAttributes();
	
	List<String> getDevices();
	
	void setDevices(ArrayList<String> devices);
	
	String getCompanyId();
	
	String getId();
	
	String getParentId();
	
	void setParentId(String parentId);
	
	boolean getScheduleImmediate();
	String getScheduleTime();
	
	AlgorithmStatus getStatus();
	long getTimeWindowCount4Training();
	
	long getTimeWindowInMinutes();
	String getTitle();
	
	void setAlgorithmType(AlgorithmType algorithmType);
	void setAttribute(String attributeName, String attributeValue);
	
	void setAttributes(HashMap<String, String> attributes);
	void setCompanyId(String companyId);
	
	void setId(String id);
	void setScheduleImmediate(boolean immediate);
	
	void setScheduleTime(String chronTime);
	void setStatus(AlgorithmStatus status);
	
	void setTimeWindowCount4Training(long timeWindowCount4Training);
	void setTimeWindowInMinutes(long timeWindowInMinutes); 
	
	void setTitle(String title);
	
	String getTrainingStartTime();
	void setTrainingStartTime(String time);
	
	long getTrainingSessionCountPerSchedule();
	void setTrainingSessionCountPerSchedule(long count);
	

}
