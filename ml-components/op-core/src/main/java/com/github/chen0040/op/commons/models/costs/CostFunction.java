package com.github.chen0040.op.commons.models.costs;

import com.github.chen0040.op.commons.models.solutions.NumericSolutionFactory;
import com.github.chen0040.op.commons.services.GradientEstimation;

/**
 * Created by memeanalytics on 12/8/15.
 */

/// <summary>
/// function whose fitness is always the smaller the better
/// </summary>
public abstract class CostFunction implements Cloneable
{

    protected int dimensionCount; //number of dimension
    protected double[] lowerBounds; //lower bound vector
    protected double[] upperBounds; //upper bound vector
    protected Object constraint;
    //algorithm variables
    protected int mEvaluationCount; //number of solvers

    public void copy(CostFunction rhs){
        dimensionCount = rhs.dimensionCount;
        lowerBounds = rhs.lowerBounds == null ? null : rhs.lowerBounds.clone();
        upperBounds = rhs.upperBounds == null ? null : rhs.upperBounds.clone();
        constraint = rhs.constraint;
        mEvaluationCount = rhs.mEvaluationCount;
    }

    public CostFunction(int dimension_count, double lower_bound, double upper_bound)
    {
        lowerBounds = new double[dimension_count];
        upperBounds = new double[dimension_count];

        for (int i = 0; i < dimension_count; ++i)
        {
            lowerBounds[i] = lower_bound;
            upperBounds[i] = upper_bound;
        }

        initialize(dimension_count);
    }

    public CostFunction()
    {

    }

    public Object getConstraint() {
        return constraint;
    }

    public void setConstraint(Object constraint) {
        this.constraint = constraint;
    }

    public int getDimensionCount()
    {
        return dimensionCount;
    }

    public double getLowerBoundAtIndex(int dimension)
    {
        return lowerBounds[dimension];
    }

    public void setLowerBoundAtIndex(int dimension, double lower_bound)
    {
        lowerBounds[dimension] = lower_bound;
    }

    public double getUpperBoundAtIndex(int dimension)
    {
        return upperBounds[dimension];
    }

    public void setUpperBoundAtIndex(int dimension, double upper_bound)
    {
        upperBounds[dimension] = upper_bound;
    }

    public int getEvaluationcount()
    {
        return mEvaluationCount;
    }

    protected  double _evaluate(double[] solution)
    {
        mEvaluationCount++;
        return 0;
    }

    public double evaluate(double[] solution)
    {
        double objective_value = _evaluate(solution);

        boolean within_bounds = true;

        for (int i = 0; i < dimensionCount; ++i)
        {
            if ((solution[i] < lowerBounds[i]) || (solution[i] > upperBounds[i]))
            {
                within_bounds = false;
                break;
            }
        }

        if (!within_bounds)
        {
            objective_value = Double.MAX_VALUE;
        }

        return objective_value;
    }

    public double evaluate(double x)
    {
        double[] solution = new double[1];
        solution[0] = x;
        return evaluate(solution);
    }

    protected  void _calcGradient(double[] solution, double[] grad)
    {
        GradientEstimation.calcGradient(solution, grad, new CostEvaluationMethod() {
            public double apply(double[] x, double[] lowerBounds, double[] upperBounds, Object constraint) {
                return evaluate(x);
            }
        }, lowerBounds, upperBounds, constraint);
        mEvaluationCount += dimensionCount;
    }

    public void calcGradient(double[] solution, double[] grad)
    {
        _calcGradient(solution, grad);
    }

    public double calcGradient(double x)
    {
        double[] solution = new double[1];
        solution[0] = x;
        double[] grad = new double[1];
        _calcGradient(solution, grad);
        return grad[0];
    }

    public boolean isOutOfBounds(double[] solution)
    {
        boolean result = false;
        for (int i = 0; i < dimensionCount; ++i)
        {
            if (solution[i] < lowerBounds[i])
            {
                result = true;
                break;
            }
            else if (solution[i] > upperBounds[i])
            {
                result = true;
                break;
            }
        }

        return result;
    }

    protected void initialize(int dimension_count)
    {
        dimensionCount = dimension_count;

        //initialize local variables
        mEvaluationCount = 0;
    }

    public double[] createRandomSolution()
    {
        return NumericSolutionFactory.create(lowerBounds, upperBounds);
    }

}

