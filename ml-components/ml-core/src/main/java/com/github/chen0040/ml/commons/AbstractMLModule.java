package com.github.chen0040.ml.commons;

import com.github.chen0040.ml.commons.discrete.AbstractAttributeValueDiscretizer;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 22/8/15.
 */
public abstract class AbstractMLModule implements MLModule {
    private Map<String, Double> attributes;
    private String id;
    private String projectId;
    private String prototype;
    private String description;
    private Date created;
    private Date updated;
    private String createdBy;
    private String updatedBy;
    private String title;
    private MLModuleOutputType outputType;
    private boolean isShell;
    private boolean isLabelRequiredInBatchUpdate;
    private int statusCode;
    private String statusInfo;
    protected AttributeValueDiscretizer inputDiscretizer;
    private IntelliContext modelSource = new IntelliContext();


    public IntelliContext getModelSource() {
        return modelSource;
    }

    public void setModelSource(IntelliContext modelSource) {
        if(modelSource != null) {
            this.modelSource = new IntelliContext();
            this.modelSource.copyWithoutTuples(modelSource);
        }
    }

    public abstract Object clone();

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

    public void setInputDiscretizer(AttributeValueDiscretizer avd){
        inputDiscretizer = avd;
    }

    public AttributeValueDiscretizer getInputDiscretizer(){
        return inputDiscretizer;
    }

    public void copy(MLModule rhs){
        attributes = new HashMap<>();
        AbstractMLModule rhs2 = (AbstractMLModule)rhs;
        for(String attrname : rhs2.attributes.keySet()){
            attributes.put(attrname, rhs2.attributes.get(attrname));
        }
        this.id= rhs2.id;
        projectId = rhs2.projectId;
        prototype = rhs2.prototype;

        modelSource = rhs2.modelSource.clone();
        description = rhs2.description;
        created = rhs2.created;
        updated = rhs2.updated;
        createdBy = rhs2.createdBy;
        updatedBy = rhs2.updatedBy;
        title = rhs2.title;
        outputType = rhs2.outputType;
        isShell = rhs2.isShell;
        isLabelRequiredInBatchUpdate = rhs2.isLabelRequiredInBatchUpdate;
        statusCode = rhs2.statusCode;
        statusInfo = rhs2.statusInfo;

        inputDiscretizer = rhs2.inputDiscretizer == null ? null : (AttributeValueDiscretizer)((AbstractAttributeValueDiscretizer)rhs2.inputDiscretizer).clone();
    }

    public boolean getIsLabelRequiredInBatchUpdate() {
        return isLabelRequiredInBatchUpdate;
    }

    public void setIsLabelRequiredInBatchUpdate(boolean isLabelReqInBatchUpdate) {
        this.isLabelRequiredInBatchUpdate = isLabelReqInBatchUpdate;
    }

    public AbstractMLModule(){
        id = null;
        attributes = new HashMap<String, Double>();
        prototype = this.getClass().getCanonicalName();
        outputType = MLModuleOutputType.PredictedLabels;
        isLabelRequiredInBatchUpdate = false;
        statusCode = 200;
        statusInfo = "";
    }

    public boolean isEvaluationSupported(IntelliContext batch){
        boolean supported = true;
        try{
            evaluate(batch.tupleAtIndex(0), batch);
        }catch(Exception ex){
            supported = false;
        }

        return supported;
    }

    public boolean getIsShell(){
        return isShell;
    }

    public void setIsShell(boolean isShell){
        this.isShell = isShell;
    }

    public MLModuleOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(MLModuleOutputType outputType) {
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

    public abstract BatchUpdateResult batchUpdate(IntelliContext batch);

    public abstract double evaluate(IntelliTuple tuple, IntelliContext context);

    public Map<String, Double> getAttributes()
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

    public IntelliTuple newTuple(Map<Integer, String> row, TupleTransformRules rules){
        return getModelSource().newTuple(row, rules);
    }

    public IntelliTuple newTuple(Map<Integer, String> row){
        return getModelSource().newTuple(row);
    }

    public IntelliTuple newTuple(DataRow row, TupleTransformRules rules){
        return getModelSource().newTuple(row, rules);
    }

    public IntelliTuple newTuple(DataRow row){
        return getModelSource().newTuple(row);
    }

    public IntelliTuple newTuple() {
        return getModelSource().newTuple();
    }


    public DataRow toDataRow(IntelliTuple tuple){
        return getModelSource().toDataRow(tuple);
    }
}
