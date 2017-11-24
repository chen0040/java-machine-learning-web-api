package com.github.chen0040.op.search.cgs;

import com.github.chen0040.op.commons.OPModule;
import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.misc.LineSearchResult;
import com.github.chen0040.op.commons.models.misc.TerminationEvaluationMethod;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionFactory;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;
import com.github.chen0040.op.commons.services.LineSearch;
import com.github.chen0040.op.commons.models.costs.GradientEvaluationMethod;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class NonlinearCGSearch extends OPModule {
    private BetaFormula betaFormula;

    @Override
    public void copy(OPModule rhs){
        super.copy(rhs);

        NonlinearCGSearch rhs2 = (NonlinearCGSearch)rhs;
        betaFormula = rhs2.betaFormula;
    }

    @Override
    public Object clone(){
        NonlinearCGSearch clone = new NonlinearCGSearch();
        clone.copy(this);

        return clone;
    }

    public NonlinearCGSearch(){
        betaFormula = BetaFormula.FletcherReeves;
    }

    public void setBetaFormula(BetaFormula formula){
        this.betaFormula = formula;
    }


    @Override
    public NumericSolution minimize(double[] x_0, final CostEvaluationMethod evaluate, GradientEvaluationMethod calc_gradient, TerminationEvaluationMethod should_terminate, Object constraint) {




        NumericSolution best_solution = new NumericSolution();

        int dimension = x_0.length;

        double[] x = x_0.clone();
        double fx = evaluate.apply(x, getLowerBounds(), getUpperBounds(), constraint);

        double[] Vfx = new double[dimension];
        calc_gradient.apply(x, Vfx, getLowerBounds(), getUpperBounds(), constraint);

        double[] deltaX=new double[dimension];
        double[] deltaX_prev = new double[dimension];


        for(int d=0; d < dimension; ++d)
        {
            deltaX[d]=-Vfx[d];
        }

        LineSearchResult result = LineSearch.search(x, fx, deltaX, evaluate, calc_gradient, getLowerBounds(), getUpperBounds(), constraint);

        double alpha = result.alpha();
        double[] x_next = result.x();
        double fx_next = result.fx();

        double beta = 0;

        double[] s = new double[dimension];

        for (int d = 0; d < dimension; ++d)
        {
            s[d] = deltaX[d];
        }

        int iteration = 0;
        NumericSolutionUpdateResult state = null;
        while(!should_terminate.shouldTerminate(state, iteration))
        {
            for (int d = 0; d < dimension; ++d)
            {
                deltaX_prev[d] = deltaX[d];
                x[d] = x_next[d];
            }

            calc_gradient.apply(x, Vfx, getLowerBounds(), getUpperBounds(), constraint);
            for (int d = 0; d < dimension; ++d)
            {
                deltaX[d] = -Vfx[d];
            }

            beta = computeBeta(deltaX, deltaX_prev, s);

            for (int d = 0; d < dimension; ++d)
            {
                s[d] = deltaX[d] + beta * s[d];
            }

            result = LineSearch.search(x, fx, s, evaluate, calc_gradient, getLowerBounds(), getUpperBounds(), constraint);

            x_next = result.x();
            fx_next = result.fx();
            alpha = result.alpha();

            state = best_solution.tryUpdateSolution(x_next, fx_next);

            if(state.improved())
            {
                notifySolutionUpdated(best_solution, state, iteration);
            }
            step(new NumericSolution(x_next, fx_next), state, iteration);
            iteration++;
        }

        return best_solution;
    }

    public double[] randomize(double[] x){
        return NumericSolutionFactory.mutate(x, 5);
    }

    public double computeBeta(double[] deltaX, double[] deltaX_prev, double[] s)
    {
        double beta = 0;
        int dimension=deltaX.length;
        if (betaFormula == BetaFormula.FletcherReeves)
        {
            double num1 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num1 += Math.pow(deltaX[d], 2);
            }
            double num2 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num2 += Math.pow(deltaX_prev[d], 2);
            }
            if (num2 != 0)
            {
                beta = num1 / num2;
            }
        }
        else if (betaFormula == BetaFormula.HestenesStiefel)
        {
            double num1 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num1+=deltaX[d] * (deltaX[d] - deltaX_prev[d]);
            }
            double num2 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num2 += Math.pow(deltaX_prev[d], 2);
            }
            if (num2 != 0)
            {
                beta = num1 / num2;
            }
        }
        else if (betaFormula == BetaFormula.PolakRebiere)
        {
            double num1 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num1 += deltaX[d] * (deltaX[d] - deltaX_prev[d]);
            }
            double num2 = 0;
            for (int d = 0; d < dimension; ++d)
            {
                num2 += s[d] * (deltaX[d] - deltaX_prev[d]);
            }
            if (num2 != 0)
            {
                beta = num1 / num2;
            }
        }
        return beta;
    }
}
