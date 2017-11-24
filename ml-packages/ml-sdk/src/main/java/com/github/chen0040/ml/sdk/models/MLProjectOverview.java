package com.github.chen0040.ml.sdk.models;

import com.github.chen0040.ml.sdk.models.workflows.MLWorkflow;

import java.util.ArrayList;

/**
 * Created by memeanalytics on 4/9/15.
 */
public class MLProjectOverview {
    public ArrayList<MLModuleOverview> modules = new ArrayList<MLModuleOverview>();
    public ArrayList<BasicBatchOverview> batches = new ArrayList<BasicBatchOverview>();
    public String id;
    public String name;
    public String description;
    public TestingModel testing = new TestingModel();
    public ArrayList<TestingModel> testingModes = new ArrayList<TestingModel>();
    public MLWorkflow workflow = new MLWorkflow();
    public int statusCode;
    public String statusInfo;

    public MLProjectOverview(){
        id = "";
        name = "";
        description = "";
        statusCode = 200;
        statusInfo = "";
        testingModes.add(new TestingModel(TestingMode.ByTrainingPortion));
        testingModes.add(new TestingModel(TestingMode.CrossValidation));
        testingModes.add(new TestingModel(TestingMode.NoTraining));
        testingModes.add(new TestingModel(TestingMode.TestOnTraining));
    }
}
