package com.github.chen0040.ml.sdk.models;

/**
 * Created by memeanalytics on 4/9/15.
 */
public class MLTestCase {
    public TestingModel testingModel;
    public String moduleId;
    public String batchId;
    public String moduleName;
    public String batchName;
    public int statusCode;
    public String statusInfo;

    public MLTestCase(){
        statusCode = 200;
        statusInfo = "";
    }
}
