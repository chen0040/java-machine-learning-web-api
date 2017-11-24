package com.github.chen0040.op.search.naive;

import com.github.chen0040.op.commons.OPModule;
import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.costs.GradientEvaluationMethod;
import com.github.chen0040.op.commons.models.misc.TerminationEvaluationMethod;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;

/**
 * Created by memeanalytics on 23/8/15.
 */
public class SweepingSearch extends OPModule {

    private int intervalCount;

    @Override
    public void copy(OPModule rhs){
        super.copy(rhs);

        SweepingSearch rhs2 = (SweepingSearch)rhs;
        intervalCount = rhs2.intervalCount;
    }

    @Override
    public Object clone(){
        SweepingSearch clone = new SweepingSearch();
        clone.copy(this);

        return clone;
    }

    public SweepingSearch(){
        intervalCount = 100;
    }

    @Override
    public NumericSolution minimize(double[] x_0, final CostEvaluationMethod evaluate, GradientEvaluationMethod calc_gradient, TerminationEvaluationMethod should_terminate, Object constraint) {

        NumericSolution best_solution = new NumericSolution();

        double[] x = x_0.clone();
        double fx = evaluate.apply(x, getLowerBounds(), getUpperBounds(), constraint);

        best_solution.tryUpdateSolution(x, fx);

        int iteration = 0;
        NumericSolutionUpdateResult state = null;

        int m = x.length;

        int L = 0;
        for(int i=0; i < m; ++i){
            L = L * intervalCount + (intervalCount - 1);
        }

        while(L > 0)
        {
            double[] x_next = create(getLowerBounds(), getUpperBounds(), L);
            double fx_next = evaluate.apply(x_next, getLowerBounds(), getUpperBounds(), constraint);

            state = best_solution.tryUpdateSolution(x_next, fx_next);

            if(state.improved())
            {
                notifySolutionUpdated(best_solution, state, iteration);
            }
            step(new NumericSolution(x_next, fx_next), state, iteration);

            L--;
            iteration++;
        }

        return best_solution;
    }

    private double[] create(double[] lower, double[] upper, int L){
        int m = lower.length;
        int[] indices = new int[m];
        for(int i=0; i < m; ++i){
            int index = L % intervalCount;
            indices[i] = index;
            L = L / intervalCount;
        }

        double[] x = new double[m];
        for(int i=0; i < m; ++i){
            x[i] = lower[i] + indices[i] * (upper[i] - lower[i]) / intervalCount;
        }
        return x;
    }
}
