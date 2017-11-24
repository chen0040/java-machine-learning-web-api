package com.github.chen0040.ml.sdk.models.workflows;

/**
 * Created by root on 9/8/15.
 */
public class MLWorkflowConnectionPin {
    public int nodeID;
    public int connectorIndex;

    public MLWorkflowConnectionPin clone(){
        MLWorkflowConnectionPin clone = new MLWorkflowConnectionPin();
        clone.nodeID = nodeID;
        clone.connectorIndex = connectorIndex;

        return clone;
    }
}
