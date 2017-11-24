package com.github.chen0040.ml.clustering.em;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by memeanalytics on 18/8/15.
 */

/// <summary>
/// Expectation Maximization Clustering, Note that this is a soft clustering method
/// </summary>
public class EMClustering extends AbstractClustering {

    private static final Random random = new Random();
    protected double sigma0;
    protected int clusterCount;
    protected double[][] expectactionMatrix;
    protected double[][] clusters;
    private int maxIters = 2000;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        EMClustering rhs2 = (EMClustering)rhs;
        sigma0 = rhs2.sigma0;
        clusterCount = rhs2.clusterCount;
        expectactionMatrix = rhs2.expectactionMatrix == null ? null : rhs2.expectactionMatrix.clone();
        clusters = rhs2.clusters == null ? null : rhs2.clusters.clone();
        maxIters = rhs2.maxIters;
    }

    @Override
    public Object clone(){
        EMClustering clone = new EMClustering();
        clone.copy(this);

        return clone;
    }


    public EMClustering()
    {
        sigma0 = 0.01;
        clusterCount = 10;
    }

    public int getMaxIters() {
        return maxIters;
    }

    public void setMaxIters(int maxIters) {
        this.maxIters = maxIters;
    }

    public double getDistance(double[] x1, double[] x2){
        int n = x1.length;
        double sum = 0;
        for(int i=0; i < n; ++i){
            sum += (x1[i] - x2[i]) * (x1[i] - x2[i]);
        }

        return Math.sqrt(sum);
    }

    public double calcExpectation(IntelliContext context, IntelliTuple tuple, int clusterIndex)
    {
        double[] P=new double[clusterCount];
        double sum = 0;
        for (int k = 0; k < clusterCount; ++k)
        {
            double distance = getDistance(context.toNumericArray(tuple), clusters[k]);

            P[k] = Math.exp(-Math.sqrt(distance) / (2 * sigma0 * sigma0));
            sum += P[k];
        }
        return P[clusterIndex] / sum;
    }

    private void initializeCluster(IntelliContext batch, int n){
        HashSet<Integer> indexList = new HashSet<Integer>();
        int m = batch.tupleCount();
        if(m < clusterCount * 3){
            clusterCount = Math.min(m, clusterCount);
            for(int i=0; i < clusterCount; ++i){
                indexList.add(i);
            }
        }else {

            while (indexList.size() < clusterCount) {
                int r = random.nextInt(m);
                if (!indexList.contains(r)) {
                    indexList.add(r);
                }
            }
        }

        clusters = new double[clusterCount][];
        for(int i=0; i < clusterCount; ++i){
            clusters[i] = new double[n];
        }

        // initialize cluster centers
        int clusterIndex = 0;
        for(Integer i : indexList) {
            double[] center = clusters[clusterIndex];
            IntelliTuple t = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(t);
            for(int j=0; j < n; ++j){
                center[j] = x[j];
            }
            clusterIndex++;
        }
    }

    private void initializeEM(IntelliContext batch){
        int m = batch.tupleCount();
        expectactionMatrix = new double[m][];
        for(int i=0; i< clusterCount; ++i){
            expectactionMatrix[i] = new double[clusterCount];
        }
    }

    @Override
    public int getCluster(IntelliTuple tuple) {
        if(tuple.getPredictedLabelOutput() != null){
            return Integer.parseInt(tuple.getPredictedLabelOutput());
        }

        return -1;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch)
    {
        int m = batch.tupleCount();

        int n = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        initializeCluster(batch, n);
        initializeEM(batch);

        for (int iteration = 0; iteration < maxIters; ++iteration)
        {
            //expectation step
            for (int i = 0; i < m; ++i)
            {
                for (int k = 0; k < clusterCount; ++k)
                {
                    expectactionMatrix[i][k] = calcExpectation(batch, batch.tupleAtIndex(i), k);
                }
            }

            //maximization step
            for (int k = 0; k < clusterCount; ++k)
            {
                for (int d = 0; d < n; ++d)
                {
                    double denom = 0;
                    double num = 0;
                    for (int i = 0; i < m; ++i)
                    {
                        IntelliTuple tuple = batch.tupleAtIndex(i);
                        double[] x = batch.toNumericArray(tuple);
                        num += expectactionMatrix[i][k] * x[d];
                        denom += expectactionMatrix[i][k];
                    }
                    double mu = num / denom;
                    clusters[k][d] = mu;
                }
            }

        }

        for(int i=0; i < m; ++i){
            int max_k = -1;

            double max_expectation = Double.MIN_VALUE;
            for (int k = 0; k < clusterCount; ++k)
            {
                double expectation = expectactionMatrix[i][k];
                if (expectation > max_expectation)
                {
                    max_expectation = expectation;
                    max_k = k;
                }
            }
            IntelliTuple tuple = batch.tupleAtIndex(i);
            tuple.setPredictedLabelOutput(String.format("%d", max_k));
        }

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}


