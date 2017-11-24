package com.github.chen0040.op.commons.benchmarks;

import com.github.chen0040.op.commons.models.costs.CostFunction;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class RosenbrockSaddleCostFunction extends CostFunction {

    @Override
    public Object clone(){
        RosenbrockSaddleCostFunction clone = new RosenbrockSaddleCostFunction();
        clone.copy(this);
        return clone;
    }

    public RosenbrockSaddleCostFunction()
    {
        super(2, -2.048, 2.048);
    }

    @Override
    protected void _calcGradient(double[] solution, double[] grad)
    {
        double x0 = solution[0];
        double x1 = solution[1];
        grad[0] = 400 * (x0 * x0 - x1) * x0 - 2 * (1 - x0);
        grad[1] = -200 * (x0 * x0 - x1);
    }

    @Override
    protected double _evaluate(double[] solution)
    {
        double x0 = solution[0];
        double x1 = solution[1];

        double cost =100 * Math.pow(x0 * x0 - x1, 2) + Math.pow(1 - x0, 2);
        return cost;
    }




}
