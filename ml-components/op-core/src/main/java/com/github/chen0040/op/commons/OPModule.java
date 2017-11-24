package com.github.chen0040.op.commons;

import com.github.chen0040.op.commons.events.NumericSolutionIterateListener;
import com.github.chen0040.op.commons.events.NumericSolutionUpdatedListener;
import com.github.chen0040.op.commons.models.costs.CostEvaluationMethod;
import com.github.chen0040.op.commons.models.costs.CostFunction;
import com.github.chen0040.op.commons.models.costs.GradientEvaluationMethod;
import com.github.chen0040.op.commons.models.misc.TerminationEvaluationMethod;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;
import com.github.chen0040.op.commons.services.GradientEstimation;
import com.github.chen0040.op.commons.services.SearchListenerService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Random;

/**
 * Created by memeanalytics on 12/8/15.
 */
public abstract class OPModule implements Cloneable {
    private static Random rand = new Random();
    private double[] upperBounds;
    private double[] lowerBounds;
    private SearchListenerService listenerService;

    public void copy(OPModule rhs){
        upperBounds = rhs.upperBounds == null ? null : rhs.upperBounds.clone();
        lowerBounds = rhs.lowerBounds == null ? null : rhs.lowerBounds.clone();
    }

    @Override
    public Object clone(){
        throw new NotImplementedException();
    }

    public OPModule(){
        listenerService = new SearchListenerService();
    }

    public void addIterateListener(NumericSolutionIterateListener listener){
        listenerService.addIterateListener(listener);
    }

    public void removeIterateListener(NumericSolutionIterateListener listener){
        listenerService.removeIterateListener(listener);
    }

    public void addUpdateListener(NumericSolutionUpdatedListener listener){
        listenerService.addUpdateListener(listener);
    }

    public void removeUpdateListener(NumericSolutionUpdatedListener listener){
        listenerService.removeUpdateListener(listener);
    }

    public double[] getUpperBounds() {
        return upperBounds;
    }

    public void setUpperBounds(double[] upperBounds) {
        this.upperBounds = upperBounds;
    }

    public double[] getLowerBounds() {
        return lowerBounds;
    }

    public void setLowerBounds(double[] lowerBounds) {
        this.lowerBounds = lowerBounds;
    }

    public abstract NumericSolution minimize(double[] x_0, final CostEvaluationMethod evaluate, GradientEvaluationMethod calc_gradient, TerminationEvaluationMethod should_terminate, Object constraint);

    public NumericSolution minimize(double[] x_0, final CostFunction f, final int iterations){
        return minimize(x_0, new CostEvaluationMethod() {
            public double apply(double[] x, double[] lowerBounds, double[] upperBounds, Object constraint) {
                return f.evaluate(x);
            }
        }, new GradientEvaluationMethod() {
            public void apply(double[] x, double[] Vf, double[] lowerBounds, double[] upperBounds, Object constraint) {
                f.calcGradient(x, Vf);
            }
        }, new TerminationEvaluationMethod() {
            public boolean shouldTerminate(NumericSolutionUpdateResult state, int iteration) {
                return iteration >= iterations;
            }
        }, null);
    }

    public NumericSolution minimize(double[] x_0, final CostEvaluationMethod evaluate, final int maxIterations){
        GradientEvaluationMethod calc_grad = new GradientEvaluationMethod() {
            public void apply(double[] x, double[] Vf, double[] lowerBounds, double[] upperBounds, Object constraint) {
                GradientEstimation.calcGradient(x, Vf, evaluate, lowerBounds, upperBounds, constraint);
            }
        };

        TerminationEvaluationMethod should_terminate = new TerminationEvaluationMethod() {
            public boolean shouldTerminate(NumericSolutionUpdateResult state, int iteration) {
                return iteration >= maxIterations;
            }
        };

        return minimize(x_0, evaluate, calc_grad, should_terminate, null);
    }

    public NumericSolution minimize(final CostEvaluationMethod evaluate, final int maxIterations){
        double[] x_0 = createSolution();

        return minimize(x_0, evaluate, maxIterations);
    }

    private double[] createSolution(){
        int dimension = lowerBounds.length;
        double[] x_0 = new double[dimension];

        for(int i=0; i < dimension; ++i){
            x_0[i] = lowerBounds[i] * rand.nextDouble() * (upperBounds[i] - lowerBounds[i]);
        }

        return x_0;
    }

    protected void notifySolutionUpdated(NumericSolution solution, NumericSolutionUpdateResult state, int iteration){
        listenerService.notifySolutionUpdated(solution, state, iteration);
    }

    protected void step(NumericSolution solution, NumericSolutionUpdateResult state, int iteration){
        listenerService.step(solution, state, iteration);
    }


}
