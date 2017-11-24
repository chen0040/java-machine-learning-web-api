package com.github.chen0040.op.commons.models.solutions;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class NumericSolutionUpdateResult {
    private double improvement;
    private boolean improved;

    public NumericSolutionUpdateResult(double improvement, boolean improved){
        this.improvement = improvement;
        this.improved = improved;
    }

    public boolean improved(){
        return improved;
    }

    public double improvement(){
        return improvement;
    }
}
