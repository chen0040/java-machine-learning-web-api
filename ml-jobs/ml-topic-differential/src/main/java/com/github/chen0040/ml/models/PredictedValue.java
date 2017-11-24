package com.github.chen0040.ml.models;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Predicted result value
 * Created by xiaoyong on 19/10/15.
 * @see <a href="https://github.com/kairosdb/kairosdb/blob/develop/src/docs/CassandraSchema.rst">https://github.com/kairosdb/kairosdb/blob/develop/src/docs/CassandraSchema.rst</a>
 * <a href="http://opentsdb.net/docs/build/html/user_guide/backends/hbase.html">http://opentsdb.net/docs/build/html/user_guide/backends/hbase.html</a>
 */
public class PredictedValue implements Jsonable {
    /**
     * milli seconds since epoch
     */
    long date=System.currentTimeMillis();
    /**
     * output value(double type, such as IsolationForest will output 1.0(Anomaly) or 0.0(Normal))
     */
    double doubleValue=0.0;
    /**
     * output value(string type, such as IsolationForest will output 'Anomaly' or 'Normal')
     */
    String stringValue="output";
    /**
     * internal value(algorithm compute result)
     */
    double internalValue=0.0;
    /**
     * each feature's value,value can be list.tojson(),map.tojson(),double.tostring() etc.
     *
     */
    Map<Feature,String> featureValuesMap =new HashMap<>();
    /**
     * feature value's type Canonical Name, such as :java.util.HashMap,java.util.ArrayList,java.lang.String,java.lang.Double etc.
     */
    String featureValueType=Double.class.getCanonicalName();


    public PredictedValue() {
    }

    public PredictedValue(double doubleValue, String stringValue) {
        this.doubleValue = doubleValue;
        this.stringValue = stringValue;
    }

    public PredictedValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public PredictedValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }
    //region getter setter

    /**
     *
     * @return the number of milliseconds since EPOCH
     */
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
    public void setDate(Date date){
        this.date=date.getTime();
    }
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public double getInternalValue() {
        return internalValue;
    }

    public void setInternalValue(double internalValue) {
        this.internalValue = internalValue;
    }

    public String getFeatureValueType() {
        return featureValueType;
    }

    public void setFeatureValueType(String featureValueType) {
        this.featureValueType = featureValueType;
    }
    public void setFeatureValueType(Class<?> featureValueType) {
        this.featureValueType = featureValueType.getCanonicalName();
    }
    public double getDoubleValue() {
        return doubleValue;
    }

    public Map<Feature, String > getFeatureValuesMap() {
        return featureValuesMap;
    }

    public void setFeatureValuesMap(Map<Feature, String> featureValuesMap) {
        this.featureValuesMap = featureValuesMap;
    }

    public void addFeature(String featureName, String data){
        Feature f = new Feature();
        f.setName(featureName);
        featureValuesMap.put(f, data);
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }
    //endregion
    public static PredictedValue valueOf(double pow) {
        return new PredictedValue(pow);
    }
    public static PredictedValue valueOf(String pow) {
        return new PredictedValue(pow);
    }

    @Override
    public String toJson() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.config(SerializerFeature.QuoteFieldNames, false);
        serializer.config(SerializerFeature.UseSingleQuotes, true);
        serializer.write(this);
        return serializer.getWriter().toString();
    }
}
