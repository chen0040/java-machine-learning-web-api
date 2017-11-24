package com.github.chen0040.ml.spark.core.discrete;

import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.core.*;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by memeanalytics on 18/8/15.
 */
public abstract class AbstractAttributeValueDiscretizer implements AttributeValueDiscretizer {
    private String id;
    private String projectId;
    private String prototype;
    private String createdBy;
    private String updatedBy;
    private Date created;
    private Date updated;
    private String title;
    private String description;
    private SparkMLOutputType outputType;
    private HashMap<String, Double> attributes;
    private boolean isShell;
    private boolean isLabelReqInBatchUpdate;

    protected HashSet<Integer> columnsToDiscretize = new HashSet<Integer>();

    public void makeColumnDiscrete(int columnIndex){
        columnsToDiscretize.add(columnIndex);
    }

    public boolean coverColumn(int columnIndex){
        return columnsToDiscretize.contains(columnIndex);
    }

    public void copy(SparkMLModule rhs){

        AbstractAttributeValueDiscretizer rhs2 = (AbstractAttributeValueDiscretizer) rhs;
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
        attributes.clear();
        for(String key : rhs2.attributes.keySet()){
            attributes.put(key, rhs2.attributes.get(key));
        }
        isShell = rhs2.isShell;
        isLabelReqInBatchUpdate = rhs2.isLabelReqInBatchUpdate;

        columnsToDiscretize.clear();
        for(Integer index : rhs2.columnsToDiscretize){
            columnsToDiscretize.add(index);
        }
    }

    public abstract Object clone();

    public AbstractAttributeValueDiscretizer() {
        prototype = this.getClass().getCanonicalName();
        outputType = SparkMLOutputType.VectorQuantization;
        attributes = new HashMap<String, Double>();
    }

    public boolean getIsLabelRequiredInBatchUpdate() {
        return isLabelReqInBatchUpdate;
    }

    public void setIsLabelRequiredInBatchUpdate(boolean isLabelReqInBatchUpdate) {
        this.isLabelReqInBatchUpdate = isLabelReqInBatchUpdate;
    }

    public boolean getIsShell(){
        return isShell;
    }

    public void setIsShell(boolean isShell){
        this.isShell = isShell;
    }

    public SparkMLOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(SparkMLOutputType outputType) {
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

    public abstract int discretize(double value, int index);

    public abstract BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch);

    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        throw new NotImplementedException();
    }

    public PredictionResult evaluate(SparkMLTuple tuple){
        return evaluate(tuple, null);
    }

    public boolean isEvaluationSupported(JavaRDD<SparkMLTuple> batch){
        return false;
    }


    public abstract SparkMLTuple discretize(SparkMLTuple tuple);

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

    public JavaRDD<SparkMLTuple> discretize(JavaRDD<SparkMLTuple> batch){
        JavaRDD<SparkMLTuple> rdd = batch.map(new Function<SparkMLTuple, SparkMLTuple>() {
            public SparkMLTuple call(SparkMLTuple tuple) throws Exception {
                return discretize(tuple);
            }
        });

        return rdd;
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
