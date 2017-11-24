package com.github.chen0040.ml.sdk.models.workflows;

/**
 * Created by root on 9/8/15.
 */
public class MLWorkflowNodePin {
    public String name;
    public String id;

    public MLWorkflowNodePin clone(){
        MLWorkflowNodePin clone = new MLWorkflowNodePin();
        clone.name = name;
        clone.id = id;
        return clone;
    }
}
