package com.github.chen0040.op.commons.models.misc;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class LineSearchResult {
    private double[] x;
    private double fx;
    private double alpha;
    private boolean success;

    public LineSearchResult(double[] x, double fx, double alpha, boolean success){
        this.x = x;
        this.fx = fx;
        this.alpha = alpha;
        success = true;
    }

    public double[] x(){
        return x;
    }

    public double fx(){
        return fx;
    }

    public double alpha(){
        return alpha;
    }

    public boolean success(){
        return success;
    }
}
