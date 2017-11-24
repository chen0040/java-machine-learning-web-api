package com.github.chen0040.ml.viewmodels;


import com.github.chen0040.ml.models.AlgorithmView;
import com.github.chen0040.ml.enums.AlgorithmViewType;

public class AlgorithmDashboardPanelRequest implements AlgorithmView {
	
	private static final long serialVersionUID = 1L;
	private long timeWindowInMinutes;
	private long startTime;
	private long endTime;
	private String moduleId;
	private String chartType;
	private String algorithmId;
	private String id;
	private String title = "";
	private AlgorithmViewType viewType = AlgorithmViewType.Chart;
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public AlgorithmDashboardPanelRequest(){
		
	}
	
	public long getTimeWindowInMinutes() {
		return timeWindowInMinutes;
	}
	public void setTimeWindowInMinutes(long timeWindowInMinutes) {
		this.timeWindowInMinutes = timeWindowInMinutes;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	@Override
	public AlgorithmViewType getViewType() {
		return viewType;
	}

	@Override
	public void setViewType(AlgorithmViewType viewType) {
		this.viewType = viewType;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public void setAlgorithmId(String algorithmId) {
		this.algorithmId = algorithmId;
	}
	
	
}
