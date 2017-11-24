package com.github.chen0040.ml.svm.regression;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.regressions.AbstractRegression;
import com.github.chen0040.ml.commons.regressions.PredictionResult;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.svm.libsvm.*;
import com.github.chen0040.ml.svm.libsvm.*;

import java.util.Vector;
import java.util.function.Function;

/**
 * Created by memeanalytics on 13/8/15.
 * Link: http://web.cs.iastate.edu/~honavar/smo-svm.pdf
 */
public class SVR extends AbstractRegression {

    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s) {}
    };
    private svm_parameter param;
    private int cross_validation;
    private svm_model model;
    private boolean quiet;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        SVR rhs2 = (SVR) rhs;
        param = rhs2.param == null ? null : (svm_parameter)rhs2.param.clone();
        cross_validation = rhs2.cross_validation;
        model = rhs2.model == null ? null : (svm_model)rhs2.model.clone();
        if(model != null) model.param = param;
        quiet = rhs2.quiet;
    }

    @Override
    public Object clone(){
        SVR clone = new SVR();
        clone.copy(this);

        return clone;
    }

    public SVR(){
        svm_print_interface print_func = null;	// default printing to stdout

        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.NU_SVR;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        cross_validation = 0;

        svm.svm_set_print_string_function(svm_print_null);
        this.quiet = true;
    }

    public SVMType getSVMType(){
        if(param.svm_type == svm_parameter.EPSILON_SVR){
            return SVMType.epsilon;
        }else{
            return SVMType.nu;
        }
    }

    public void setSVMType(SVMType type){
        switch (type){
            case nu:
                param.svm_type = svm_parameter.NU_SVR;
                break;
            case epsilon:
                param.svm_type = svm_parameter.EPSILON_SVR;
        }
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public svm_parameter getParameters(){
        return param;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double[] x0 = context.toNumericArray(tuple);
        int n = x0.length;

        svm_node[] x = new svm_node[n];
        for(int j=0; j < n; j++)
        {
            x[j] = new svm_node();
            x[j].index = j+1;
            x[j].value = x0[j];
        }

        double v = svm.svm_predict(model, x);
        return v;
    }

    @Override
    public PredictionResult predict(IntelliTuple tuple) {
        double p = evaluate(tuple, getModelSource());
        PredictionResult result = new PredictionResult();
        result.predictedOutput = p;
        return result;
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasNumericOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        if(this.quiet){
            svm.svm_set_print_string_function(svm_print_null);
        }else{
            svm.svm_set_print_string_function(null);
        }

        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        int m = batch.tupleCount();
        for(int i=0; i < m; ++i)
        {
            IntelliTuple tuple = batch.tupleAtIndex(i);

            double[] x0 = batch.toNumericArray(tuple);
            int n = x0.length;


            vy.add(tuple.getNumericOutput());
            svm_node[] x = new svm_node[n];
            for(int j=0; j < n; j++)
            {
                x[j] = new svm_node();
                x[j].index = j+1;
                x[j].value = x0[j];
            }

            if(n>0) max_index = Math.max(max_index, x[n-1].index);

            vx.addElement(x);
        }

        svm_problem prob = new svm_problem();
        prob.l = m;
        prob.x = new svm_node[prob.l][];
        for(int i=0;i<prob.l;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y = new double[prob.l];
        for(int i=0;i<prob.l;i++)
            prob.y[i] = vy.elementAt(i);

        if(param.gamma == 0 && max_index > 0)
            param.gamma = 1.0/max_index;


        model = svm.svm_train(prob, param);

        return new BatchUpdateResult();
    }

    public enum SVMType{
        nu,
        epsilon
    }



}
