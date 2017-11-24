package com.github.chen0040.ml.svm.smo;

import Jama.Matrix;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.classifiers.AbstractBinaryClassifier;
import com.github.chen0040.ml.svm.smo.models.SMOSVMModel;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.ml.svm.smo.kernels.KernelFunction;
import com.github.chen0040.ml.svm.KernelFunctionType;
import com.github.chen0040.ml.svm.smo.kernels.LinearKernelFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 13/8/15.
 * Link:
 * http://web.cs.iastate.edu/~honavar/smo-svm.pdf
 * http://www.jmlr.org/papers/volume2/manevitz01a/manevitz01a.pdf
 * http://www.bu.edu/phpbin/cise/download.php?publication_id=1113
 */
public class SMOSVC extends AbstractBinaryClassifier {

    private double tol = 0.001;
    private int max_passes = 5;
    private double C = 0.1;
    private KernelFunction kernelFunction;
    private SMOSVMModel model;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        SMOSVC rhs2 = (SMOSVC)rhs;
        tol = rhs2.tol;
        max_passes = rhs2.max_passes;
        C = rhs2.C;
        kernelFunction = rhs2.kernelFunction == null ? null : rhs2.kernelFunction.clone();
        model = rhs2.model == null ? null : (SMOSVMModel)rhs2.model.clone();
        if(model != null) model.kernelFunction = kernelFunction;
    }

    @Override
    public Object clone(){
        SMOSVC clone = new SMOSVC();
        clone.copy(this);

        return clone;
    }

    public SMOSVC(){
        super();
        this.kernelFunction = new LinearKernelFunction();
    }

    public SMOSVC(String classLabel){
        super(classLabel);
        this.kernelFunction = new LinearKernelFunction();
    }

    public SMOSVC(String classLabel, KernelFunction kernelFunction){
        super(classLabel);
        this.kernelFunction = kernelFunction;
    }



    public void setKernelFunction(KernelFunction f){
        this.kernelFunction = f;
    }

    public double getC(){
        return C;
    }

    public void setC(double val){
        C = val;
    }

    public double getTol(){
        return tol;
    }

    public void setTol(double value){
        tol = value;
    }

    public int getMaxPasses(){
        return max_passes;
    }

    public void setMaxPasses(int value){
        max_passes = value;
    }


    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] x = context.toNumericArray(tuple);
        int n = x.length;
        int m = 1;

        Matrix X = new Matrix(1, n);
        for(int i=0; i < n; ++i){
            X.set(0, i, x[i]);
        }


        // Dataset
        Matrix p = MatrixService.zeros(m, 1);
        Matrix pred = MatrixService.zeros(m, 1);

        if(model.kernelFunction.getType()== KernelFunctionType.LINEAR) {
            // We can use the weights and bias directly if working with the
            // linear kernel
            p = MatrixService.plus(X.times(model.w), model.b);
        } else if(model.kernelFunction.getType()==KernelFunctionType.RBF) {
            // Vectorized RBF Kernel
            // This is equivalent to computing the kernel on every pair of examples
            Matrix X1 = MatrixService.sum(MatrixService.pow(X, 2), 2);
            Matrix X2 = MatrixService.sum(MatrixService.pow(model.X, 2), 2).transpose();
            Matrix K = MatrixService.bsxfun_plus(X1, MatrixService.bsxfun_plus(X2, X.times(model.X.transpose()).times(-2)));
            K = MatrixService.pow(model.kernelFunction.kernel(1, 0), K);
            K = MatrixService.bsxfun_times(model.y.transpose(), K);
            K = MatrixService.bsxfun_times(model.alphas.transpose(), K);
            p = MatrixService.sum(K, 2);
        } else {
            //Other Non -linear kernel
            for(int i = 0; i < m; ++i) {
                double prediction = 0;

                for(int j = 0; j < model.X.getRowDimension(); ++j) {
                    prediction = prediction +
                        model.alphas.get(j, 0) * model.y.get(j, 0) *
                        model.kernelFunction.kernel(MatrixService.row(X, i).transpose(), MatrixService.column(model.X, j).transpose());
                }
                p.set(i, 0, prediction + model.b);
            }
        }

        return p.get(0, 0);

    }

    @Override
    public boolean isInClass(IntelliTuple tuple, IntelliContext context) {
        double p = evaluate(tuple, getModelSource());
        return p > 0;
    }

    public SMOSVMModel model(){
        return model;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        int m = batch.tupleCount();
        if (m == 0) return new BatchUpdateResult(new Exception("IntelliContext contains no data"));

        if(getPositiveClassLabel()==null){
            scan4ClassLabel(batch);
        }

        int n = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        Matrix X = new Matrix(m, n);
        Matrix Y = new Matrix(m, 1);
        for (int i = 0; i < m; ++i)
        {
            IntelliTuple rec = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(rec);
            for (int d = 0; d < n; ++d)
            {
                X.set(i, d, x[d]);
            }
        }

        for (int i = 0; i < m; ++i)
        {
            IntelliTuple rec = batch.tupleAtIndex(i);
            Y.set(i, 0, BinaryClassifierUtils.isInClass(rec, getPositiveClassLabel()) ? 1 : -1);
        }

        Matrix K = null;
        if(kernelFunction.getType() == KernelFunctionType.LINEAR) {
            // Vectorized computation for the Linear Kernel
            // This is equivalent to computing the kernel on every pair of examples
            K = X.times(X.transpose());
        } else if (kernelFunction.getType() == KernelFunctionType.RBF) {
            // Vectorized RBF Kernel
            // This is equivalent to computing the kernel on every pair of examples
            Matrix X2 = MatrixService.sum(MatrixService.pow(X, 2), 2); // column vector

            K = MatrixService.bsxfun_plus(X2, MatrixService.bsxfun_plus(X2.transpose(), (X.times(X.transpose())).times(-2)));
            K = MatrixService.pow(kernelFunction.kernel(1, 0), K);
        } else {
            // Pre-compute the Kernel Matrix
            // The following can be slow due to the lack of vectorization
            K = MatrixService.zeros(m);
            for(int i = 0; i < m; ++i) {
                for(int j = i; j < m; ++j) {
                    K.set(i, j, kernelFunction.kernel(MatrixService.row(X, i).transpose(), MatrixService.row(X, j).transpose()));
                    K.set(j, i, K.get(i, j)); //the matrix is symmetric
                }
            }
        }

        Matrix alphas = MatrixService.zeros(m, 1);
        double b = 0;
        Matrix E = MatrixService.zeros(m, 1);
        int passes = 0;
        double eta = 0;
        double L = 0;
        double H = 0;

        while(passes < max_passes)
        {
            int num_changed_alphas = 0;
            for(int i = 0; i < m; ++i)
            {
                // Calculate Ei = f(x(i)) - y(i) using (2).
                // E(i) = b + sum (X(i, :) * (repmat(alphas.*Y,1,n).*X)') - Y(i);
                E.set(i, 0, b + MatrixService.vecSum(alphas.arrayTimes(Y).arrayTimes(MatrixService.column(K, i))) - Y.get(i, 0));

                if ((Y.get(i, 0) * E.get(i, 0) < -tol && alphas.get(i, 0) < C) ||
                        (Y.get(i, 0)*E.get(i, 0) > tol && alphas.get(i, 0) > 0)) {
                    // In practice, there are many heuristics one can use to select
                    // the i and j. In this simplified code, we select them randomly.
                    int j = (int) Math.ceil((m-1) * MatrixService.rand());
                    while (j == i) {
                        // Make sure i \neq j
                        j = (int) Math.ceil((m-1) * MatrixService.rand());
                    }

                    // Calculate Ej = f(x(j)) - y(j) using (2).
                    E.set(j, 0, b + MatrixService.vecSum(alphas.arrayTimes(Y).arrayTimes(MatrixService.column(K, j))) - Y.get(j, 0));

                    // Save old alphas
                    double alpha_i_old = alphas.get(i, 0);
                    double alpha_j_old = alphas.get(j, 0);

                    // Compute L and H by (10) or (11).
                    if (Y.get(i, 0) == Y.get(j, 0)) {
                        L = Math.max(0, alphas.get(j, 0) + alphas.get(i, 0) - C);
                        H = Math.min(C, alphas.get(j, 0) + alphas.get(i, 0));
                    } else {
                        L = Math.max(0, alphas.get(j, 0) - alphas.get(i, 0));
                        H = Math.min(C, C + alphas.get(j, 0) - alphas.get(i, 0));
                    }

                    if (L == H) {
                        // continue to next i.
                        continue;
                    }

                    // Compute eta by (14).
                    eta = 2 * K.get(i, j) - K.get(i, i) - K.get(j, j);
                    if (eta >= 0) {
                        // continue to next i.
                        continue;
                    }

                    // Compute and clip new value for alpha j using (12) and (15).
                    alphas.set(j, 0, alphas.get(j, 0) - (Y.get(j, 0) * (E.get(i, 0) - E.get(j, 0))) / eta);

                    // Clip
                    alphas.set(j, 0, Math.min(H, alphas.get(j, 0)));
                    alphas.set(j, 0, Math.max(L, alphas.get(j, 0)));

                    // Check if change in alpha is significant
                    if (Math.abs(alphas.get(j, 0) - alpha_j_old) < tol) {
                        // continue to next i.
                        // replace anyway
                        alphas.set(j, 0, alpha_j_old);
                        continue;
                    }

                    // Determine value for alpha i using (16).
                    alphas.set(i, 0, alphas.get(i, 0) + Y.get(i, 0) * Y.get(j, 0) * (alpha_j_old - alphas.get(j, 0)));

                    // Compute b1 and b2 using (17) and (18) respectively.
                    double b1 = b - E.get(i, 0)
                            - Y.get(i, 0) * (alphas.get(i, 0) - alpha_i_old) * K.get(i, j)
                            - Y.get(j, 0) * (alphas.get(j, 0) - alpha_j_old) * K.get(i, j);
                    double b2 = b - E.get(j, 0)
                            - Y.get(i, 0) * (alphas.get(i, 0) - alpha_i_old) * K.get(i, j)
                            - Y.get(j, 0) * (alphas.get(j, 0) - alpha_j_old) * K.get(j, j);

                    // Compute b by (19).
                    if (0 < alphas.get(i, 0) && alphas.get(i, 0) < C) {
                        b = b1;
                    } else if (0 < alphas.get(j, 0) && alphas.get(j, 0) < C) {
                        b = b2;
                    } else {
                        b = (b1 + b2) / 2;
                    }

                    num_changed_alphas = num_changed_alphas + 1;

                }

            }

            if (num_changed_alphas == 0) {
                passes = passes + 1;
            } else {
                passes = 0;
            }
        }

        List<Integer> idx = new ArrayList<Integer>();

        for(int i=0; i < m; ++i){
            if(alphas.get(i, 0) > 0) {
                idx.add(i);
            }
        }

        model = new SMOSVMModel();
        model.X= MatrixService.filterRows(X, idx);
        model.y= MatrixService.filterRows(Y, idx);
        model.kernelFunction = kernelFunction;
        model.b = b;
        model.alphas= MatrixService.filterRows(alphas, idx);
        model.w = ((alphas.arrayTimes(Y).transpose()).times(X)).transpose();

        return new BatchUpdateResult();
    }



}
