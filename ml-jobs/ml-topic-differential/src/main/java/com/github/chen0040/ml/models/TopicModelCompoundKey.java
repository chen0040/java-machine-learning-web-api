package com.github.chen0040.ml.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TopicModelCompoundKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8444498224490147285L;

	
	@Column
	private String moduleId;

	@Column
	private long captureTime;

	public TopicModelCompoundKey() {}
	public TopicModelCompoundKey(String moduleId, long captureTime) {
		this.moduleId = moduleId;
		this.captureTime = captureTime;
	}
	public long getCaptureTime() {
		return captureTime;
	}
	public void setCaptureTime(long captureTime) {
		this.captureTime = captureTime;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	
}
