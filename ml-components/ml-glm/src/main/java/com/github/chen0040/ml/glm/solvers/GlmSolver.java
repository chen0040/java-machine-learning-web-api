package com.github.chen0040.ml.glm.solvers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.regressions.PredictionResult;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.regressions.AbstractRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class GlmSolver extends AbstractRegression {

    private static final Logger logger = LoggerFactory.getLogger(GlmSolver.class);

    protected Glm solver;
    protected GlmDistributionFamily distributionFamily;
    protected GlmSolverType solverType;
    protected GlmCoefficients coefficients;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        GlmSolver rhs2 = (GlmSolver)rhs;
        solver = rhs2.solver == null ? null : (Glm)rhs2.solver.clone();
        distributionFamily = rhs2.distributionFamily;
        solverType = rhs2.solverType;
        coefficients = rhs2.coefficients == null ? null : (GlmCoefficients)rhs2.coefficients.clone();
    }

    @Override
    public Object clone(){
        GlmSolver clone = new GlmSolver();
        clone.copy(this);

        return clone;
    }

    public GlmSolver(GlmSolverType solverType, GlmDistributionFamily distributionFamily){
        this.solverType = solverType;
        this.distributionFamily = distributionFamily;
        this.coefficients = new GlmCoefficients();
    }

    public GlmSolver(){
        this(GlmSolverType.GlmIrls,GlmDistributionFamily.Normal);
    }

    public GlmDistributionFamily getDistributionFamily() {
        return distributionFamily;
    }

    public void setDistributionFamily(GlmDistributionFamily distributionFamily) {
        this.distributionFamily = distributionFamily;
    }

    public GlmSolverType getSolverType() {
        return solverType;
    }

    public void setSolverType(GlmSolverType solverType) {
        this.solverType = solverType;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] x0 = context.toNumericArray(tuple);
        double[] x = new double[x0.length+1];
        x[0]=1;
        for(int i=0; i < x0.length; ++i){
            x[i+1] = x0[i];
        }

        return solver.predict(x);
    }

    protected Glm createSolver(double[][] A, double[] b){
        if(solverType == GlmSolverType.GlmNaive){
            return new Glm(distributionFamily, A, b);
        } else if(solverType == GlmSolverType.GlmIrlsQr){
            return new GlmIrlsQrNewton(distributionFamily, A, b);
        } else if(solverType == GlmSolverType.GlmIrls){
            return new GlmIrls(distributionFamily, A, b);
        } else if(solverType == GlmSolverType.GlmIrlsSvd){
            return new GlmIrlsSvdNewton(distributionFamily, A, b);
        }
        return null;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        int m = batch.tupleCount();
        double[][] X = new double[m][];

        coefficients.setDescriptors(batch.levelsInDoubleArray(batch.tupleAtIndex(0)));

        double[] y = new double[m];
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x_i = batch.toNumericArray(tuple);

            double[] x_prime = new double[x_i.length+1];
            x_prime[0] = 1;
            for(int j=0; j < x_i.length; ++j) {
                x_prime[j+1] = x_i[j];
            }
            X[i] = x_prime;


            y[i] = tuple.getNumericOutput();
        }

        solver = createSolver(X, y);
        double[] x_best = solver.solve();


        if(x_best == null){
            return new BatchUpdateResult(new Exception("The solver failed"));
        }else{
            coefficients.setValues(x_best);
            return new BatchUpdateResult();
        }
    }


    public GlmStatistics showStatistics(){
        return solver != null ? solver.getStatistics() : null;
    }

    public GlmCoefficients getCoefficients(){
        return coefficients;
    }

    @Override
    public PredictionResult predict(IntelliTuple tuple) {
        double value = evaluate(tuple, getModelSource());
        PredictionResult result = new PredictionResult();
        result.predictedOutput = value;
        return result;
    }
}
