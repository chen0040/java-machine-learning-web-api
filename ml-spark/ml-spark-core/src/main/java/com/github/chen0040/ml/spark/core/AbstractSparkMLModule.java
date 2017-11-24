package com.github.chen0040.ml.spark.core;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by memeanalytics on 22/8/15.
 */
public abstract class AbstractSparkMLModule implements SparkMLModule {
    private HashMap<String, Double> attributes;
    private String id;
    private String projectId;
    private String prototype;
    private String modelSourceId;
    private String description;
    private Date created;
    private Date updated;
    private String createdBy;
    private String updatedBy;
    private String title;
    private SparkMLOutputType outputType;
    private boolean isShell;
    private boolean isLabelReqInBatchUpdate;

    public abstract Object clone();

    public void copy(SparkMLModule rhs){
        attributes = new HashMap<String, Double>();
        AbstractSparkMLModule rhs2 = (AbstractSparkMLModule)rhs;
        for(String attrname : rhs2.attributes.keySet()){
            attributes.put(attrname, rhs2.attributes.get(attrname));
        }
        this.id= rhs2.id;
        projectId = rhs2.projectId;
        prototype = rhs2.prototype;
        modelSourceId = rhs2.modelSourceId;
        description = rhs2.description;
        created = rhs2.created;
        updated = rhs2.updated;
        createdBy = rhs2.createdBy;
        updatedBy = rhs2.updatedBy;
        title = rhs2.title;
        outputType = rhs2.outputType;
        isShell = rhs2.isShell;
        isLabelReqInBatchUpdate = rhs2.isLabelReqInBatchUpdate;
    }

    public boolean getIsLabelRequiredInBatchUpdate() {
        return isLabelReqInBatchUpdate;
    }

    public void setIsLabelRequiredInBatchUpdate(boolean isLabelReqInBatchUpdate) {
        this.isLabelReqInBatchUpdate = isLabelReqInBatchUpdate;
    }

    public AbstractSparkMLModule(){
        id = null;
        attributes = new HashMap<String, Double>();
        prototype = this.getClass().getCanonicalName();
        outputType = SparkMLOutputType.PredictedLabels;
        isLabelReqInBatchUpdate = false;
    }

    public void setIsShell(boolean isShell){
        this.isShell = isShell;
    }

    public boolean getIsShell(){
        return isShell;
    }

    public SparkMLOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(SparkMLOutputType outputType) {
        this.outputType = outputType;
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

    public String getModelSourceId() {
        return modelSourceId;
    }

    public void setModelSourceId(String modelSourceId) {
        this.modelSourceId = modelSourceId;
    }


    public HashMap<String, Double> getAttributes()
    {
        return attributes;
    }

    public String getPrototype(){
        return prototype;
    }

    public double getAttribute(String name){
        if(attributes.containsKey(name)){
            return attributes.get(name);
        }
        return 0;
    }

    public void setAttribute(String name, double value){
        attributes.put(name, value);
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getProjectId(){
        return this.projectId;
    }

    public void setProjectId(String projectId){
        this.projectId = projectId;
    }


    public abstract BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch);
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc){
        throw new NotImplementedException();
    }
    public PredictionResult evaluate(SparkMLTuple tuple){
        return evaluate(tuple, null);
    }
}
