package com.github.chen0040.ml.commons.discrete;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.MLModuleOutputType;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 18/8/15.
 */
public abstract class AbstractAttributeValueDiscretizer implements AttributeValueDiscretizer {
    protected TupleAttributeLevelSource attributeLevelSource;
    private String id;
    private String projectId;
    private String prototype;
    private String createdBy;
    private String updatedBy;
    private Date created;
    private Date updated;
    private String title;
    private String description;
    private MLModuleOutputType outputType;
    private HashMap<String, Double> attributes;
    private boolean isShell;
    private boolean isLabelRequiredInBatchUpdate;
    private int statusCode;
    private String statusInfo;
    private IntelliContext modelSource = new IntelliContext();

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

    public IntelliContext getModelSource() {
        return modelSource;
    }

    public void setModelSource(IntelliContext modelSource) {
        if(modelSource != null) {
            this.modelSource.copyWithoutTuples(modelSource);
        }
    }

    public void copy(MLModule rhs){
        AbstractAttributeValueDiscretizer rhs2 = (AbstractAttributeValueDiscretizer) rhs;
        modelSource = rhs2.modelSource.clone();
        attributeLevelSource = rhs2.attributeLevelSource;
        id = rhs2.id;
        projectId = rhs2.projectId;
        prototype = rhs2.prototype;
        createdBy = rhs2.createdBy;
        updatedBy = rhs2.updatedBy;
        created = rhs2.created;
        updated = rhs2.updated;
        title = rhs2.title;
        description = rhs2.description;
        outputType = rhs2.outputType;
        statusCode = rhs2.statusCode;
        statusInfo = rhs2.statusInfo;
        attributes.clear();
        for(String key : rhs2.attributes.keySet()){
            attributes.put(key, rhs2.attributes.get(key));
        }
        isShell = rhs2.isShell;
        isLabelRequiredInBatchUpdate = rhs2.isLabelRequiredInBatchUpdate;
    }

    public abstract Object clone();

    public AbstractAttributeValueDiscretizer() {
        attributeLevelSource = new TupleAttributeLevelSource();
        prototype = this.getClass().getCanonicalName();
        outputType = MLModuleOutputType.VectorQuantization;
        attributes = new HashMap<String, Double>();
        statusCode = 200;
        statusInfo = "";
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

    public IntelliTuple newTuple(){
        return getModelSource().newTuple();
    }

    public DataRow toDataRow(IntelliTuple tuple){
        return getModelSource().toDataRow(tuple);
    }

    public boolean getIsLabelRequiredInBatchUpdate() {
        return isLabelRequiredInBatchUpdate;
    }

    public void setIsLabelRequiredInBatchUpdate(boolean isLabelReqInBatchUpdate) {
        this.isLabelRequiredInBatchUpdate = isLabelReqInBatchUpdate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrototype() {
        return prototype;
    }

    public TupleAttributeLevelSource getAttributeLevelSource() {
        return attributeLevelSource;
    }

    public abstract int discretize(double value, int index);

    public abstract BatchUpdateResult batchUpdate(IntelliContext batch);

    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }

    public boolean isEvaluationSupported(IntelliContext batch){
        return false;
    }

    protected abstract IntelliTuple discretize(IntelliTuple tuple, TupleAttributeLevelSource levels);

    public IntelliTuple discretize(IntelliTuple tuple) {
        return discretize(tuple, attributeLevelSource);
    }

    public HashMap<String, Double> getAttributes() {
        return attributes;
    }

    public double getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return 0;
    }

    public void setAttribute(String name, double value) {
        attributes.put(name, value);
    }

    public IntelliContext discretize(IntelliContext batch){
        IntelliContext newBatch = new IntelliContext();
        newBatch.copyWithoutTuples(batch);
        for(int i = 0; i < batch.tupleCount(); ++i){
            newBatch.add(discretize(batch.tupleAtIndex(i)));
        }
        return newBatch;
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
}
