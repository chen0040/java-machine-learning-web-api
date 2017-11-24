package com.github.chen0040.ml.sdk.models.workflows;

import java.util.ArrayList;

/**
 * Created by root on 9/8/15.
 */
public class MLWorkflowNode {
    public String name;
    public int id;
    public double x;
    public double y;
    public double width;
    public ArrayList<MLWorkflowNodePin> inputConnectors = new ArrayList<MLWorkflowNodePin>();
    public ArrayList<MLWorkflowNodePin> outputConnectors = new ArrayList<MLWorkflowNodePin>();

    public String modelId;
    public MLWorkflowNodeModelType modelType = MLWorkflowNodeModelType.Module;



    public MLWorkflowNode clone(){
        MLWorkflowNode clone = new MLWorkflowNode();
        clone.name = name;
        clone.id = id;
        clone.x = x;
        clone.y = y;
        clone.modelId = modelId;
        clone.modelType = modelType;
        for(int i = 0; i < inputConnectors.size(); ++i){
            clone.inputConnectors.add(inputConnectors.get(i).clone());
        }
        for(int i=0; i < outputConnectors.size(); ++i){
            clone.outputConnectors.add(outputConnectors.get(i).clone());
        }

        return clone;
    }
}
