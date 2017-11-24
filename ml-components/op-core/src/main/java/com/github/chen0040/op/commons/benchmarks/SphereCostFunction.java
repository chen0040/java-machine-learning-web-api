package com.github.chen0040.op.commons.benchmarks;

import com.github.chen0040.op.commons.models.costs.CostFunction;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class SphereCostFunction extends CostFunction {

    @Override
    public Object clone(){
        SphereCostFunction clone = new SphereCostFunction();
        clone.copy(this);

        return clone;
    }


    public SphereCostFunction(){
        super(3, -5.12, 5.12);
    }

    @Override
    protected void _calcGradient(double[] x, double[] Vf){
        int dimension = x.length;
        for (int i = 0; i < dimension; ++i)
        {
            Vf[i] = 2 * x[i];
        }
    }

    @Override
    protected double _evaluate(double[] solution)
    {
        int dimension = solution.length;
        double cost = 0;
        for (int i = 0; i < dimension; ++i)
        {
            double x = solution[i];
            cost += x * x;
        }
        return cost;
    }



}
