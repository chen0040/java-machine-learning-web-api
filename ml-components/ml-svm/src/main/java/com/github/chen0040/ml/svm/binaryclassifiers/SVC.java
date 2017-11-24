package com.github.chen0040.ml.svm.binaryclassifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.classifiers.AbstractBinaryClassifier;
import com.github.chen0040.ml.commons.regressions.PredictionResult;
import com.github.chen0040.ml.svm.libsvm.*;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.ml.svm.libsvm.*;

import java.util.Vector;
import java.util.function.Function;

/**
 * Created by memeanalytics on 13/8/15.
 * Link: http://web.cs.iastate.edu/~honavar/smo-svm.pdf
 */
public class SVC extends AbstractBinaryClassifier {

    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s) {}
    };
    private svm_parameter parameters;
    private int cross_validation;
    private svm_model model;
    private boolean quiet;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        SVC rhs2 = (SVC)rhs;
        parameters = rhs2.parameters == null ? null : (svm_parameter)rhs2.parameters.clone();
        cross_validation = rhs2.cross_validation;
        model = rhs2.model == null ? null : (svm_model)rhs2.model.clone();
        quiet = rhs2.quiet;
    }

    @Override
    public Object clone(){
        SVC clone = new SVC();
        clone.copy(this);

        return clone;
    }

    public SVC(String label){
        super(label);

        init();
    }

    public SVC(){
        super();

        init();
    }

    public static svm_print_interface getSvm_print_null() {
        return svm_print_null;
    }

    public static void setSvm_print_null(svm_print_interface svm_print_null) {
        SVC.svm_print_null = svm_print_null;
    }

    public int getCross_validation() {
        return cross_validation;
    }

    public void setCross_validation(int cross_validation) {
        this.cross_validation = cross_validation;
    }

    public svm_model getModel() {
        return model;
    }

    public void setModel(svm_model model) {
        this.model = model;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public SVMType getSVMType(){
        if(parameters.svm_type == svm_parameter.C_SVC){
            return SVMType.C;
        }else{
            return SVMType.nu;
        }
    }

    public void setSVMType(SVMType type){
        switch (type){

            case C:
                parameters.svm_type = svm_parameter.C_SVC;
                break;
            case nu:
                parameters.svm_type = svm_parameter.NU_SVC;
                break;
        }
    }

    private void init(){
        svm_print_interface print_func = null;	// default printing to stdout

        parameters = new svm_parameter();
        // default values
        parameters.svm_type = svm_parameter.C_SVC;
        parameters.kernel_type = svm_parameter.RBF;
        parameters.degree = 3;
        parameters.gamma = 0;	// 1/num_features
        parameters.coef0 = 0;
        parameters.nu = 0.5;
        parameters.cache_size = 100;
        parameters.C = 1;
        parameters.eps = 1e-3;
        parameters.p = 0.1;
        parameters.shrinking = 1;
        parameters.probability = 0;
        parameters.nr_weight = 0;
        parameters.weight_label = new int[0];
        parameters.weight = new double[0];
        cross_validation = 0;

        svm.svm_set_print_string_function(null);
        quiet = false;
    }

    public svm_parameter getParameters(){
        return parameters;
    }

    public void setParameters(svm_parameter parameters) {
        this.parameters = parameters;
    }

    private void info(String info){

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
    public boolean isInClass(IntelliTuple tuple, IntelliContext context) {
        double p = evaluate(tuple, context);
        PredictionResult result = new PredictionResult();
        result.predictedOutput = p;
        return p > 0;
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

        if(this.quiet){
            svm.svm_set_print_string_function(svm_print_null);
        }else{
            svm.svm_set_print_string_function(null);
        }

        Vector<Double> vy = new Vector<Double>();
        Vector<svm_node[]> vx = new Vector<svm_node[]>();
        int max_index = 0;

        int m = batch.tupleCount();

        if(getPositiveClassLabel()==null) {
            scan4ClassLabel(batch);
        }

        for(int i=0; i < m; ++i)
        {
            IntelliTuple tuple = batch.tupleAtIndex(i);

            double[] x0 = batch.toNumericArray(tuple);
            int n = x0.length;

            //System.out.println(StringHelper.toString(x0));

            vy.add(BinaryClassifierUtils.isInClass(tuple, getPositiveClassLabel()) ? 1.0 : -1.0);

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
        prob.x = new svm_node[m][];
        for(int i=0;i<m;i++)
            prob.x[i] = vx.elementAt(i);
        prob.y = new double[m];
        for(int i=0;i<m;i++)
            prob.y[i] = vy.elementAt(i);

        if(parameters.gamma == 0 && max_index > 0)
            parameters.gamma = 1.0/max_index;


        model = svm.svm_train(prob, parameters);

        return new BatchUpdateResult();
    }

    public enum SVMType{
        C,
        nu
    }



}
