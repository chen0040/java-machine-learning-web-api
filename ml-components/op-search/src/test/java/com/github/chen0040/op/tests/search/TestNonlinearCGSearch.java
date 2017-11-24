package com.github.chen0040.op.tests.search;

import com.github.chen0040.op.commons.benchmarks.RosenbrockSaddleCostFunction;
import com.github.chen0040.op.commons.benchmarks.SphereCostFunction;
import com.github.chen0040.op.commons.events.NumericSolutionUpdatedListener;
import com.github.chen0040.op.commons.models.costs.CostFunction;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;
import com.github.chen0040.op.search.cgs.BetaFormula;
import com.github.chen0040.op.search.cgs.NonlinearCGSearch;
import org.testng.annotations.Test;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class TestNonlinearCGSearch {
    @Test
    public void testSphere(){
        final BetaFormula[] formulas = {BetaFormula.FletcherReeves}; // BetaFormula.HestenesStiefel, BetaFormula.PolakRebiere};

        for(int k =0; k < formulas.length; ++k) {
            final BetaFormula formula = formulas[k];

            CostFunction f = new SphereCostFunction();
            NonlinearCGSearch search = new NonlinearCGSearch();
            search.setBetaFormula(formula);

            double[] x_0 = f.createRandomSolution();

            search.addUpdateListener(new NumericSolutionUpdatedListener() {
                public void report(NumericSolution solution, NumericSolutionUpdateResult state, int iteration) {
                    System.out.println("Beta: "+formula+"\tStep " + iteration + "\tCost:" + solution.cost());
                }
            });

            int iterations = 500;
            search.minimize(x_0, f, iterations);
        }
    }

    @Test
    public void testRosenbrockSaddle(){
        final BetaFormula[] formulas = {BetaFormula.FletcherReeves}; //, BetaFormula.HestenesStiefel, BetaFormula.PolakRebiere};

        for(int k =0; k < formulas.length; ++k) {
            final BetaFormula formula = formulas[k];

            CostFunction f = new RosenbrockSaddleCostFunction();
            NonlinearCGSearch search = new NonlinearCGSearch();
            search.setBetaFormula(formula);

            double[] x_0 = f.createRandomSolution();

            search.addUpdateListener(new NumericSolutionUpdatedListener() {
                public void report(NumericSolution solution, NumericSolutionUpdateResult state, int iteration) {
                    System.out.println("Beta: "+formula+"\tStep " + iteration + "\tCost:" + solution.cost());
                }
            });

            int iterations = 500;
            search.minimize(x_0, f, iterations);
        }

    }

}
