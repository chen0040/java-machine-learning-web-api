package com.github.chen0040.ml.ann.kohonen.clustering;

import com.github.chen0040.ml.ann.kohonen.SOFMNet;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.ann.kohonen.SOFMNeuron;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import com.github.chen0040.ml.dataprepare.transform.Standardization;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Vector;

/**
 * Created by memeanalytics on 20/8/15.
 */
public class SOFM extends AbstractClustering {
    private SOFMNet net;
    private Standardization dataNormalization;

    public static final String ROW_COUNT = "rowCount";
    public static final String COL_COUNT = "columnCount";
    public static final String ETA_0 = "eta0";

    private int rowCount(){
        return (int)getAttribute(ROW_COUNT);
    }
    private int colCount(){
        return (int)getAttribute(COL_COUNT);
    }
    private double eta0() {
        return getAttribute(ETA_0);
    }

    private void _rowCount(int rowCount){
        setAttribute(ROW_COUNT, rowCount);
    }

    public void _colCount(int colCount){
        setAttribute(COL_COUNT, colCount);
    }

    public void _eta0(double eta0){
        setAttribute(ETA_0, eta0);
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        SOFM rhs2 = (SOFM)rhs;
        net = rhs2.net == null ? null : (SOFMNet)rhs2.net.clone();
        dataNormalization = rhs2.dataNormalization == null ? null : (Standardization)rhs2.dataNormalization.clone();
    }

    @Override
    public Object clone(){
        SOFM clone = new SOFM();
        clone.copy(this);

        return clone;
    }

    public SOFM(){
        _rowCount(5);
        _colCount(5);
        _eta0(0.1);
    }

    @Override
    public int getCluster(IntelliTuple tuple) {

        double[] x = getModelSource().toNumericArray(tuple);
        x = dataNormalization.standardize(x);

        SOFMNeuron winner=net.match(x);
        return winner.output;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        int dimension = batch.toNumericArray(batch.tupleAtIndex(0)).length;
        int m = batch.tupleCount();

        dataNormalization = new Standardization(batch);

        //number of neuron rows is [Rows], number of neuron cols is [Cols], input dimension is [Input Dimension]
        net=new SOFMNet(rowCount(), colCount(), dimension);
        net.setEta0(eta0());
        net.setEpochesForSelfOrganizingPhase(1000); //SOM training consists of self-organizing phase and converging phase, this parameter specifies the number of training inputs for self-organizing phase, note that an epoch simply means a training input here

        //initialize weights on the SOM network
        Vector<Double> weight_lower_bounds=new Vector<Double>();
        Vector<Double> weight_upper_bounds=new Vector<Double>();
        for(int i=0; i < dimension; i++)
        {
            weight_lower_bounds.add(-1.0); //lower bound for each input dimension is [Weight Lower Bound]
            weight_upper_bounds.add(1.0); //upper bound for each input dimension is [Weight Upper Bound]
        }
        net.initialize(weight_lower_bounds, weight_upper_bounds);

        //set unique label for each neuron in SOM net
        for(int r=0; r < rowCount(); ++r)
        {
            for(int c=0; c < colCount(); ++c)
            {
                net.neuronAt(r, c).output = r * colCount() + c; //neuron at row r and column c will have label "[r, c]"
            }
        }

        //the SOM net can be trained using a typical 3000 training inputs, repeated the above code for other training inputs
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            x = dataNormalization.standardize(x);
            net.train(x);
        }

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
