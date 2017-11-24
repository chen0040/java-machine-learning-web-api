package com.github.chen0040.ml.glm.modelselection;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;
import com.github.chen0040.ml.commons.tuples.TupleColumn;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import com.github.chen0040.ml.glm.solvers.GlmDistributionFamily;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import com.github.chen0040.ml.glm.solvers.GlmSolverType;
import com.github.chen0040.ml.commons.tuples.*;

import java.io.File;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class ModelSelectionProblem {
    private List<TupleColumn> candidateFeatures;
    private TupleTransformRules options;
    private GlmDistributionFamily distributionFamily;
    private GlmSolverType solverType;
    private File file;
    private boolean hasHeaders;
    private String fileSplit;
    private IntelliContext defaultBatch;

    public List<TupleColumn> getCandidateFeatures() {
        return candidateFeatures;
    }

    public TupleTransformRules getOptions() {
        return options;
    }

    public GlmDistributionFamily getDistributionFamily() {
        return distributionFamily;
    }

    public GlmSolverType getSolverType() {
        return solverType;
    }

    public void removeCandidateFeature(int featureIndex){
        candidateFeatures.remove(featureIndex);
    }

    public void setDataSource(File file, String split, boolean hasHeaders){
        this.file = file;
        this.hasHeaders = hasHeaders;
        this.fileSplit = split;
    }

    public IntelliContext createDataSource(TupleAttributeLevelSource levels, TupleTransformRules options){
        return new IntelliContext(file, fileSplit, levels, options, hasHeaders);
    }

    public IntelliContext createDefaultDataSource(){
        if(defaultBatch == null) {
            defaultBatch = new IntelliContext(file, fileSplit, hasHeaders);
        }
        return defaultBatch;
    }

    public double[] getExpectedOutputs(){
        IntelliContext records = createDefaultDataSource();
        int m = records.tupleCount();

        double[] outcomes = new double[m];
        for (int i = 0; i < m; ++i)
        {
            IntelliTuple tuple = records.tupleAtIndex(i);
            if(tuple.hasNumericOutput()) {

                outcomes[i] = tuple.getNumericOutput();
            }else {
                return null;
            }
        }
        return outcomes;
    }

    public SolverPackage buildSolver(List<TupleColumn> candidateFeatures)
    {
        TupleAttributeLevelSource levels = new TupleAttributeLevelSource();
        TupleTransformRules options = new TupleTransformRules();
        for(TupleColumn c : candidateFeatures) {
            options.addColumn(c);
        }

        IntelliContext batch = createDataSource(levels, options);
        GlmSolver solver = new GlmSolver(solverType, distributionFamily);
        solver.batchUpdate(batch);

        return new SolverPackage(solver, batch);
    }

    public class SolverPackage
    {
        GlmSolver solver;
        IntelliContext batch;

        public SolverPackage(GlmSolver solver, IntelliContext batch){
            this.solver = solver;
            this.batch = batch;
        }
    }
}
