package com.github.chen0040.ml.glm.binaryclassifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.classifiers.AbstractBinaryClassifier;
import com.github.chen0040.ml.glm.solvers.*;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.ml.glm.solvers.*;

import java.util.function.Function;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class LogisticBinaryClassifier extends AbstractBinaryClassifier {
    public static final String THRESHOLD = "threshold";
    protected GlmDistributionFamily distributionFamily;
    protected GlmSolverType solverType;
    private Glm positiveSolver;
    private GlmCoefficients coefficients;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LogisticBinaryClassifier rhs2 = (LogisticBinaryClassifier)rhs;
        distributionFamily = rhs2.distributionFamily;
        solverType = rhs2.solverType;
        positiveSolver = rhs2.positiveSolver == null ? null : (Glm)rhs2.positiveSolver.clone();
        coefficients = rhs2.coefficients == null ? null : (GlmCoefficients)rhs2.coefficients.clone();
    }

    @Override
    public Object clone(){
        LogisticBinaryClassifier clone = new LogisticBinaryClassifier();
        clone.copy(this);
        return clone;
    }

    public LogisticBinaryClassifier(String classLabel)
    {
        super(classLabel);
        setAttribute(THRESHOLD, 0.6);
        init();
    }

    public LogisticBinaryClassifier(){
        super();
        init();
        setAttribute(THRESHOLD, 0.6);
    }

    private double threshold(){
        return getAttribute(THRESHOLD);
    }

    public void setThreshold(double threshold){
        this.setAttribute(THRESHOLD, threshold);
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

    private void init(){
        distributionFamily = GlmDistributionFamily.Binomial;
        solverType = GlmSolverType.GlmIrlsQr;
        coefficients = new GlmCoefficients();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] x0 = context.toNumericArray(tuple);
        double[] x = new double[x0.length+1];
        x[0]=1;
        for(int i=0; i < x0.length; ++i){
            x[i+1] = x0[i];
        }

        return positiveSolver.predict(x);
    }

    @Override
    public boolean isInClass(IntelliTuple tuple, IntelliContext context) {
        double[] x = getDoubleArray(tuple);

        double positive_score = positiveSolver.predict(x);
        //double negative_score = negativeSolver.predict(x);

        return positive_score > threshold();
    }

    public double getPositiveScore(IntelliTuple tuple){
        return positiveSolver.predict(getDoubleArray(tuple));
    }

    public double getNegativeScore(IntelliTuple tuple){
        return 1 - positiveSolver.predict(getDoubleArray(tuple));
    }

    private double[] getDoubleArray(IntelliTuple tuple){
        double[] x0 = getModelSource().toNumericArray(tuple);
        double[] x = new double[x0.length+1];
        x[0]=1;
        for(int i=0; i < x0.length; ++i){
            x[i+1] = x0[i];
        }

        return x;
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }
    
    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        if(getPositiveClassLabel()==null){
            scan4ClassLabel(batch);
        }

        int m = batch.tupleCount();
        double[][] X = new double[m][];

        double[] y = new double[m];
        //double[] y2 = new double[m];

        coefficients.setDescriptors(batch.levelsInDoubleArray(batch.tupleAtIndex(0)));

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x_i = batch.toNumericArray(tuple);

            double[] x_prime = new double[x_i.length+1];
            x_prime[0] = 1;
            for(int j=0; j < x_i.length; ++j) {
                x_prime[j+1] = x_i[j];
            }
            X[i] = x_prime;
            y[i] = BinaryClassifierUtils.isInClass(tuple, getPositiveClassLabel()) ? 1 : 0;
            //y2[i] = 1 - y[i];
        }

        positiveSolver = createSolver(X, y);
        double[] x_best = positiveSolver.solve();
        if(x_best == null){
            return new BatchUpdateResult(new Exception("The binary classifier solver failed"));
        }else{
            coefficients.setValues(x_best);
            return new BatchUpdateResult();
        }
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

    public GlmCoefficients getCoefficients(){
        return coefficients;
    }

    public void setCoefficients(GlmCoefficients coefficients) {
        this.coefficients = coefficients;
    }
}
