package com.github.chen0040.ml.tuning;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class MLModuleModelTunerResult {
    private double[] param_values;
    private double score;
    private String[] param_names;

    public double[] getParamValues() {
        return param_values;
    }

    public double getCost() {
        return score;
    }

    public String[] getParamNames() {
        return param_names;
    }

    public MLModuleModelTunerResult(String[] param_names, double[] param_values, double score){
        this.param_values = param_values;
        this.param_names = param_names;
        this.score = score;
    }


}
