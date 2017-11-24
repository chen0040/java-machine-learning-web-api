package com.github.chen0040.ml.glm.linear;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.glm.solvers.GlmDistributionFamily;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import com.github.chen0040.ml.glm.solvers.GlmSolverType;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class LinearModel extends GlmSolver {

    public LinearModel(){
        super(GlmSolverType.GlmIrls,GlmDistributionFamily.Normal);
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

        if(solver == null) {
            return new BatchUpdateResult(new Exception("Failed to create solver for "+solverType));
        }

        double[] x_best = solver.solve();

        if(x_best == null){
            return new BatchUpdateResult(new Exception("The solver failed"));
        }else{
            coefficients.setValues(x_best);
            return new BatchUpdateResult();
        }
    }
}
