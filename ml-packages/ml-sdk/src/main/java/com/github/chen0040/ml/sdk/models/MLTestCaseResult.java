package com.github.chen0040.ml.sdk.models;

import com.github.chen0040.ml.commons.MLModuleOutputType;

import java.util.HashMap;

/**
 * Created by memeanalytics on 4/9/15.
 */
public class MLTestCaseResult {
    public String moduleId;
    public String batchId;
    public String moduleName;
    public String batchName;
    public MLModuleOutputType resultType;
    public HashMap<String, Double> attributes = new HashMap<String, Double>();
    public String moduleType;
    public int statusCode;
    public String statusInfo;

    public MLTestCaseResult(){
        statusCode = 200;
        statusInfo = "";
    }
}
