package com.github.chen0040.ml.svm.sgd;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.AbstractBinaryClassifier;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.op.commons.OPModule;
import com.github.chen0040.op.commons.events.NumericSolutionUpdatedListener;
import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;
import com.github.chen0040.op.commons.services.SearchListenerService;
import com.github.chen0040.op.commons.events.NumericSolutionIterateListener;
import com.github.chen0040.op.search.cgs.NonlinearCGSearch;

import java.util.Random;
import java.util.function.Function;

/**
 * Created by memeanalytics on 14/8/15.
 * http://www.kyb.mpg.de/fileadmin/user_upload/files/publications/attachments/neco_%5b0%5d.pdf
 * http://www.robots.ox.ac.uk/~az/lectures/ml/lect2.pdf
 * http://ttic.uchicago.edu/~nati/Publications/PegasosMPB.pdf
 */
public class LinearSVCWithSGD extends AbstractBinaryClassifier {
    protected OPModule localSearch;
    protected int searchIterations = 2000;
    protected double C = 1;
    private double[] theta;
    private LinearSVCCostFunction f;
    private NumericSolution solution;
    private SearchListenerService searchListenerService;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LinearSVCWithSGD rhs2 = (LinearSVCWithSGD)rhs;
        localSearch = rhs2.localSearch == null ? null : (OPModule)rhs2.localSearch.clone();
        searchIterations = rhs2.searchIterations;
        C = rhs2.C;
        theta = rhs2.theta.clone();
        f = rhs2.f == null ? null : (LinearSVCCostFunction)rhs2.f.clone();
        solution = rhs2.solution.clone();
    }

    @Override
    public Object clone(){
        LinearSVCWithSGD clone = new LinearSVCWithSGD();
        clone.copy(this);

        return clone;
    }

    public LinearSVCWithSGD(String classLabel)
    {
        super(classLabel);
        init();
    }

    public LinearSVCWithSGD(){
        super();
        init();
    }

    public void setLocalSearch(OPModule localSearch) {
        if(this.localSearch != localSearch) {
            this.localSearch = localSearch;
            this.localSearch.addUpdateListener(new NumericSolutionUpdatedListener() {
                public void report(NumericSolution solution, NumericSolutionUpdateResult state, int iteration) {
                    notifySolutionUpdated(solution, state, iteration);
                }
            });
            this.localSearch.addIterateListener(new NumericSolutionIterateListener() {
                public void report(NumericSolution solution, NumericSolutionUpdateResult state, int iteration) {
                    step(solution, state, iteration);
                }
            });
        }
    }

    public double getC()
    {
        return C;
    }

    public void setC(double value){
        C = value;
    }

    protected void notifySolutionUpdated(NumericSolution solution, NumericSolutionUpdateResult state, int iteration){
        searchListenerService.notifySolutionUpdated(solution, state, iteration);
    }

    protected void step(NumericSolution solution, NumericSolutionUpdateResult state, int iteration){
        searchListenerService.step(solution, state, iteration);
    }

    private void init(){
        searchListenerService = new SearchListenerService();
        setLocalSearch(new NonlinearCGSearch());
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch)
    {
        setModelSource(batch);
        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        if(getPositiveClassLabel()==null){
            scan4ClassLabel(batch);
        }

        int sample_count = batch.tupleCount();

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;


        double[][] X = new double[sample_count][];
        int[] Y = new int[sample_count];
        for (int i = 0; i < sample_count; ++i)
        {
            IntelliTuple rec = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(rec);
            X[i] = new double[dimension+1];
            X[i][0] = 1;
            for (int d = 0; d < dimension; ++d)
            {
                X[i][d+1] = x[d];
            }
        }

        Random rand = new Random();
        double[] theta_0 = new double[dimension+1];
        for (int d = 0; d < dimension+1; ++d)
        {
            theta_0[d] = rand.nextDouble();
        }

        for (int i = 0; i < sample_count; ++i)
        {
            IntelliTuple rec = batch.tupleAtIndex(i);
            Y[i] = BinaryClassifierUtils.isInClass(rec, getPositiveClassLabel()) ? 1 : -1;
        }

        f = new LinearSVCCostFunction(X, Y, dimension+1, sample_count);
        f.C = C;

        solution = localSearch.minimize(theta_0, f, searchIterations);

        theta = solution.values();

        return new BatchUpdateResult(theta == null ? new Exception("Failed to solve the sgd SVC") : null);
    }

    public double computeCost(IntelliContext data_set)
    {
        int sample_count = data_set.tupleCount();
        if (sample_count == 0) return Double.MAX_VALUE;

        int dimension = data_set.toNumericArray(data_set.tupleAtIndex(0)).length;

        double[][] X = new double[sample_count][];
        int[] Y = new int[sample_count];
        for (int i = 0; i < sample_count; ++i)
        {
            IntelliTuple rec = data_set.tupleAtIndex(i);
            double[] x = data_set.toNumericArray(rec);
            X[i] = new double[dimension+1];
            X[i][0]=1;
            for (int d = 0; d < dimension; ++d)
            {
                X[i][d+1] = x[d];
            }
        }

        double total_error = 0;
        for (int i = 0; i < sample_count; ++i)
        {
            IntelliTuple rec = data_set.tupleAtIndex(i);
            Y[i] = BinaryClassifierUtils.isInClass(rec, getPositiveClassLabel()) ? 1 : 0;
        }

        LinearSVCCostFunction f = new LinearSVCCostFunction(X, Y, dimension+1, sample_count);
        f.C = C;

        double error = f.evaluate(theta);
        total_error += error;
        return total_error;
    }

    public boolean isInClass(IntelliTuple rec, IntelliContext context)
    {
        double p = evaluate(rec, context);
        return p > 0;
    }

    @Override
    public double evaluate(IntelliTuple rec, IntelliContext context)
    {
        double[] x0 = context.toNumericArray(rec);
        int dimension = x0.length;

        double[] x = new double[dimension+1];
        x[0] = 1;
        for(int  d=0; d < dimension; ++d){
            x[d+1] = x0[d];
        }

        double fitness = 0;
        for (int d = 0; d < dimension+1; ++d)
        {
            fitness += theta[d] * x[d];
        }

        return fitness;
    }
}
