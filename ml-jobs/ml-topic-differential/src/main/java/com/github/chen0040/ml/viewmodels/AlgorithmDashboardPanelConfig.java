package com.github.chen0040.ml.viewmodels;


import com.github.chen0040.ml.enums.AlgorithmDashboardPanelChartType;
import com.github.chen0040.ml.enums.AlgorithmDashboardPanelTimeRangeUnit;
import com.github.chen0040.ml.enums.AlgorithmType;

public class AlgorithmDashboardPanelConfig {
	
	private int timeRange = 24;
	private AlgorithmDashboardPanelTimeRangeUnit timeRangeUnit = AlgorithmDashboardPanelTimeRangeUnit.Hours;
	
	private int timeWindowSize = 10;
	private AlgorithmDashboardPanelTimeRangeUnit timeWindowSizeUnit = AlgorithmDashboardPanelTimeRangeUnit.Minutes;
	
	private AlgorithmDashboardPanelChartType chartType;
	
	private int pointCount = 40;
	
	private String moduleId = "";
	
	private String selectedOption = "";
	
	private String widgetType = "Charts";
	
	private int chartHeight = 350;
	
	private AlgorithmType algorithmType = AlgorithmType.MLAnomaly_IsolationForest;
	
	public int getChartHeight(){
		return chartHeight;
	}
	
	public void setChartHeight(int chartHeight){
		this.chartHeight = chartHeight;
	}
	
	public int getPointCount(){
		return pointCount;
	}
	
	public void setPointCount(int pointCount){
		this.pointCount = pointCount;
	}
	
	public AlgorithmDashboardPanelChartType getChartType(){
		return chartType;
	}
	
	public void setChartType(AlgorithmDashboardPanelChartType chartType){
		this.chartType = chartType;
	}
    
	public AlgorithmType getAlgorithmType() {
		return algorithmType;
	}

	public void setAlgorithmType(AlgorithmType algorithmType) {
		this.algorithmType = algorithmType;
	}

	public String getWidgetType(){
		return widgetType;
	}
	
	public void setWidgetType(String chartType){
		this.widgetType = chartType;
	}
	
	public String getSelectedOption(){
		return selectedOption;
	}
	
	public void setSelectedOption(String value){
		selectedOption = value;
	}
	
	public AlgorithmDashboardPanelTimeRangeUnit getTimeRangeUnit(){
		return timeRangeUnit;
	}
	
	public void setTimeRangeUnit(AlgorithmDashboardPanelTimeRangeUnit timeRangeUnit){
		this.timeRangeUnit = timeRangeUnit;
	}
	
	public int getTimeWindowSize(){
		return timeWindowSize;
	}
	
	public void setTimeWindowSize(int timeWindowSize){
		this.timeWindowSize = timeWindowSize;
	}
	
	public AlgorithmDashboardPanelTimeRangeUnit getTimeWindowSizeUnit(){
		return timeWindowSizeUnit;
	}
	
	public void setTimeWindowSizeUnit(AlgorithmDashboardPanelTimeRangeUnit timeWindowSizeUnit){
		this.timeWindowSizeUnit = timeWindowSizeUnit;
	}
	
	public int getTimeRange(){
		return timeRange;
	}
	
	public void setTimeRange(int timeRange){
		this.timeRange = timeRange;
	}
	
	private int estimateDurationInMinutes(){
		int units = 1;
		if(timeRangeUnit == AlgorithmDashboardPanelTimeRangeUnit.Hours){
			units = 60;
		}
		
		return timeRange * units;
	}
	
	
	
	public long estimateStartTime(long endTime){
		int durationInMilliseconds = estimateDurationInMinutes() * 60000;
		return endTime - durationInMilliseconds;
	}
	
	public long estimateTimeWindowInMinutes(){
		int units = 1;
		if(timeWindowSizeUnit == AlgorithmDashboardPanelTimeRangeUnit.Hours){
			units = 60;
		}
		return timeWindowSize * units;
	}
	
	public String getModuleId(){
		return moduleId;
	}
	
	public void setModuleId(String moduleId){
		this.moduleId = moduleId;
	}
}
