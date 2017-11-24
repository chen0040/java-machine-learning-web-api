package com.github.chen0040.ml.svm.anomaly.unsupervised;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.svm.KernelFunctionType;
import com.github.chen0040.ml.svm.libsvm.svm_parameter;
import com.github.chen0040.ml.svm.oneclass.OneClassSVM;

import java.util.function.Supplier;

/**
 * Created by memeanalytics on 14/8/15.
 */
public class OneClassSVMAnomalyDetector extends AbstractAnomalyDetecter {
    private OneClassSVM algorithm;
    public static final String THRESHOLD = "threshold";


    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        OneClassSVMAnomalyDetector rhs2 = (OneClassSVMAnomalyDetector)rhs;
        algorithm = rhs2.algorithm==null ? null : (OneClassSVM)rhs2.algorithm.clone();

        if(algorithm != null){
            algorithm.thresholdSupplier = new Supplier<Double>() {
                public Double get() {
                    return threshold();
                }
            };
        }
    }

    private double threshold(){
        return getAttribute(THRESHOLD);
    }

    @Override
    public Object clone(){
        OneClassSVMAnomalyDetector clone = new OneClassSVMAnomalyDetector();
        clone.copy(this);

        return clone;
    }

    public OneClassSVMAnomalyDetector(){
        super();

        setAttribute(THRESHOLD, 0);


        algorithm = new OneClassSVM();
        algorithm.thresholdSupplier = new Supplier<Double>() {
            public Double get() {
                return threshold();
            }
        };
    }



    public void set_nu(double nu) {
        algorithm.getParameters().nu = nu;
    }

    public void set_gamma(double gamma){
        algorithm.getParameters().gamma = gamma;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        return algorithm.isAnomaly(tuple);
    }



    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        return algorithm.evaluate(tuple, context);
    }

    public KernelFunctionType getKernelType(){
        svm_parameter param = algorithm.getParameters();
        int kernelType = param.kernel_type;
        switch (kernelType){
            case svm_parameter.LINEAR:
                return KernelFunctionType.LINEAR;
            case svm_parameter.RBF:
                return KernelFunctionType.RBF;
            case svm_parameter.SIGMOID:
                return KernelFunctionType.SIGMOID;
            case svm_parameter.POLY:
                return KernelFunctionType.POLY;
            default:
                return KernelFunctionType.RBF;
        }
    }

    public void setKernelType(KernelFunctionType kernelType){
        svm_parameter param = algorithm.getParameters();
        switch (kernelType){

            case LINEAR:
                param.kernel_type = svm_parameter.LINEAR;
                break;
            case POLY:
                param.kernel_type = svm_parameter.POLY;
                break;
            case RBF:
                param.kernel_type = svm_parameter.RBF;
                break;
            case SIGMOID:
                param.kernel_type = svm_parameter.SIGMOID;
                break;
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        BatchUpdateResult result = algorithm.batchUpdate(batch);



        return result;
    }
}
