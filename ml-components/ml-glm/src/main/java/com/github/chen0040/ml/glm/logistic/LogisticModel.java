package com.github.chen0040.ml.glm.logistic;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.glm.solvers.GlmDistributionFamily;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import com.github.chen0040.ml.glm.solvers.GlmSolverType;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class LogisticModel extends GlmSolver {

    private String classLabel;
    public String getClassLabel(){
        return classLabel;
    }

    public LogisticModel(String classLabel){
        super(GlmSolverType.GlmIrlsQr, GlmDistributionFamily.Binomial);
        this.classLabel = classLabel;
    }

    public LogisticModel(){
        super(GlmSolverType.GlmIrlsQr,GlmDistributionFamily.Binomial);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
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
            y[i] = BinaryClassifierUtils.isInClass(tuple, getClassLabel()) ? 1 : 0;
        }

        solver = createSolver(X, y);

        if(solver == null)
        {
            return new BatchUpdateResult(new Exception("Failed to create the solver for "+solverType));
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
