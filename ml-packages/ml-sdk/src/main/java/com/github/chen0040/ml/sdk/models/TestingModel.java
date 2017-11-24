package com.github.chen0040.ml.sdk.models;

/**
 * Created by memeanalytics on 4/9/15.
 */
public class TestingModel {
    public double trainingPortion;
    public int crossValidationFoldCount;
    public int crossValidationLeaveOutCount;
    public int statusCode;
    public String statusInfo;

    public TestingMode mode;

    public TestingModel(){
        trainingPortion = 0.6;
        crossValidationFoldCount = 10;
        crossValidationLeaveOutCount = 2;
        mode = TestingMode.ByTrainingPortion;

        statusCode = 200;
        statusInfo = "";
    }

    public TestingModel(TestingMode mode){
        this.mode = mode;
        trainingPortion = 0.6;
        crossValidationFoldCount = 10;
        crossValidationLeaveOutCount = 2;
    }

    @Override
    public String toString() {
        return "mode: "+mode+"\ttraining portion: "+trainingPortion;
    }
}
