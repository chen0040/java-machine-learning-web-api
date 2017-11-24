package com.github.chen0040.ml.tuning;

import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.tune.Tunable;
import com.github.chen0040.op.commons.OPModule;
import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.search.naive.RandomSearch;

import java.util.List;
import java.util.function.Function;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class MLModuleModelTuner {

    private OPModule searcher;

    public OPModule getSearcher() {
        return searcher;
    }

    public void setSearcher(OPModule searcher) {
        this.searcher = searcher;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    private int maxIterations = 1000;

    public MLModuleModelTuner(){
        searcher = new RandomSearch();
    }


    private double[] getLowerBounds(List<Tunable> parameters){
        int n = parameters.size();
        double[] lowerBounds = new double[n];
        for(int i=0; i < n; ++i){
            lowerBounds[i] = parameters.get(i).getMinValue();
        }
        return lowerBounds;
    }

    private double[] getUpperBounds(List<Tunable> parameters){
        int n = parameters.size();
        double[] upperBounds = new double[n];
        for(int i=0; i < n; ++i){
            upperBounds[i] = parameters.get(i).getMaxValue();
        }
        return upperBounds;
    }

    public MLModuleModelTunerResult tune(final MLModule algorithm, final List<Tunable> parameters, final Function<MLModule, Double> evaluate) {




        searcher.setLowerBounds(getLowerBounds(parameters));
        searcher.setUpperBounds(getUpperBounds(parameters));


        NumericSolution s = searcher.minimize(new CostEvaluationMethod() {
            public double apply(double[] x, double[] lowerBounds, double[] upperBounds, Object constraint) {

                int n = parameters.size();
                for (int i = 0; i < n; ++i) {
                    algorithm.setAttribute(parameters.get(i).getName(), x[i]);
                }
                return evaluate.apply(algorithm);
            }
        }, maxIterations);


        double[] values = s.values();
        int n = parameters.size();
        String[] param_names = new String[n];
        for (int i = 0; i < n; ++i) {
            parameters.get(i).setValue(values[i]);
            param_names[i] = parameters.get(i).getName();
        }
        double score = evaluate.apply(algorithm);


        return new MLModuleModelTunerResult(param_names, values, score);
    }
}
