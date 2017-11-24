package com.github.chen0040.ml.sdk.models.workflows;

/**
 * Created by root on 9/8/15.
 */
public class MLWorkflowConnection {
    public MLWorkflowConnectionPin source = new MLWorkflowConnectionPin();
    public MLWorkflowConnectionPin dest = new MLWorkflowConnectionPin();

    public MLWorkflowConnection clone(){
        MLWorkflowConnection clone = new MLWorkflowConnection();
        clone.source = source.clone();
        clone.dest = dest.clone();

        return clone;
    }
}
