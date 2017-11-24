package com.github.chen0040.ml.sdk.models;

import com.github.chen0040.ml.sdk.models.workflows.MLWorkflow;

import java.util.Date;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLProject {
    private String title;
    private String createdBy;
    private String updatedBy;
    private Date created;
    private Date updated;
    private String id;
    private String description;
    private MLWorkflow workflow = new MLWorkflow();
    private int statusCode;
    private String statusInfo;

    public MLProject(){
        statusCode = 200;
        statusInfo = "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public MLWorkflow getWorkflow(){
        return workflow;
    }

    public void setWorkflow(MLWorkflow workflow){
        this.workflow = workflow;
    }

    public MLProject clone(){
        MLProject clone = new MLProject();
        clone.title = title;
        clone.createdBy = createdBy;
        clone.updatedBy = updatedBy;
        clone.created = created;
        clone.updated = updated;
        clone.id = id;
        clone.description = description;
        clone.workflow = workflow.clone();
        clone.statusCode = statusCode;
        clone.statusInfo = statusInfo;
        return clone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return "Project "+id;
    }
}
