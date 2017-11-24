package com.github.chen0040.ml.gaussianmixture;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.statistics.distributions.multivariate.AbstractMultiVariateDistribution;
import com.github.chen0040.statistics.distributions.multivariate.MultiVariateDistribution;
import com.github.chen0040.statistics.distributions.multivariate.MultiVariateNormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by memeanalytics on 18/8/15.
 * Random sample consensus (RANSAC) is an iterative method to
 * estimate parameters of a mathematical model from a set of observed shrinkedData which contains outliers.
 */

public class RANSAC extends AbstractAnomalyDetecter {
    private int maxIters = 100;
    private int blockSize = 20;
    private MultiVariateDistribution model;
    private double threshold = 0.5;
    private int d; // the number of close shrinkedData values required to assert that a model fits well to shrinkedData;
    private MultiVariateDistribution bestfit;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        RANSAC rhs2 = (RANSAC)rhs;
        maxIters = rhs2.maxIters;
        blockSize = rhs2.blockSize;
        AbstractMultiVariateDistribution amd = (AbstractMultiVariateDistribution)rhs2.model;
        model = amd == null ? null : (MultiVariateDistribution)amd.clone();
        threshold = rhs2.threshold;
        d = rhs2.d;

        amd = (AbstractMultiVariateDistribution)rhs2.bestfit;
        bestfit = amd == null ? null : (MultiVariateDistribution)amd.clone();
    }

    @Override
    public Object clone(){
        RANSAC clone = new RANSAC();
        clone.copy(this);
        return clone;
    }

    public RANSAC() {
        model = new MultiVariateNormalDistribution();
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        return false;
    }

    public List<List<IntelliTuple>> randomSelect(List<IntelliTuple> batch, int n) {
        Collections.shuffle(batch);
        List<IntelliTuple> list = new ArrayList<IntelliTuple>();

        n = Math.min(batch.size(), n);
        for (int i = 0; i < n; ++i) {
            list.add(batch.get(i));
        }

        List<IntelliTuple> list2 = new ArrayList<IntelliTuple>();

        for (int i = n; i < batch.size(); ++i) {
            list2.add(batch.get(i));
        }

        List<List<IntelliTuple>> lists = new ArrayList<List<IntelliTuple>>();
        lists.add(list);
        lists.add(list2);

        return lists;
    }

    private List<IntelliTuple> toList(IntelliContext batch) {
        List<IntelliTuple> list = new ArrayList<IntelliTuple>();
        for (int i = 0; i < batch.tupleCount(); ++i) {
            list.add(batch.tupleAtIndex(i));
        }
        return list;
    }

    public List<double[]> toList(IntelliContext context, List<IntelliTuple> tuples) {
        List<double[]> list = new ArrayList<double[]>();
        for (int i = 0; i < tuples.size(); ++i) {
            IntelliTuple tuple = tuples.get(i);
            list.add(context.toNumericArray(tuple));
        }
        return list;
    }

    public List<double[]> toList(IntelliContext context, List<IntelliTuple> tuples1, List<IntelliTuple> tuples2) {
        List<double[]> list = new ArrayList<double[]>();
        for (int i = 0; i < tuples1.size(); ++i) {
            IntelliTuple tuple = tuples1.get(i);
            list.add(context.toNumericArray(tuple));
        }

        for (int i = 0; i < tuples2.size(); ++i) {
            IntelliTuple tuple = tuples2.get(i);
            list.add(context.toNumericArray(tuple));
        }

        return list;
    }

    private boolean fits(IntelliContext context, IntelliTuple point, MultiVariateDistribution model, double threshold) {
        return model.inPredictionInterval(context.toNumericArray(point), threshold);
    }

    private MultiVariateDistribution clone(MultiVariateDistribution rhs){
        if(rhs==null) return null;
        AbstractMultiVariateDistribution amd = (AbstractMultiVariateDistribution)rhs;
        return (MultiVariateDistribution)amd.clone();
    }


    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        int iterations = 0;
        this.bestfit = null;
        double besterr = Double.MAX_VALUE;
        List<IntelliTuple> data = toList(batch);
        while (iterations < maxIters) {
            List<List<IntelliTuple>> lists = randomSelect(data, blockSize);
            List<IntelliTuple> maybeinliers = lists.get(0);
            List<IntelliTuple> maybeoutliers = lists.get(1);

            MultiVariateDistribution maybemodel = clone(model);
            maybemodel.sample(toList(batch, maybeinliers));
            List<IntelliTuple> alsoinliers = new ArrayList<IntelliTuple>();

            for (IntelliTuple point : maybeoutliers) {
                if (fits(batch, point, maybemodel, threshold)) {
                    alsoinliers.add(point);
                }
            }

            if (alsoinliers.size() > d) {
                // this implies that we may have found a good model
                // now test how good it is
                MultiVariateDistribution bettermodel = clone(model);
                List<double[]> samples = toList(batch, alsoinliers, maybeinliers);
                bettermodel.sample(samples); //model parameters fitted to all points in maybeinliers and alsoinliers
                double thiserr = getError(bettermodel, samples); //a measure of how well model fits these points
                if (thiserr < besterr) {
                    this.bestfit = bettermodel;
                    besterr = thiserr;
                }
            }
            iterations++;
        }
        return new BatchUpdateResult();
    }

    // This method' implementation is my own ad-hoc implementation
    private double getError(MultiVariateDistribution bettermodel, List<double[]> samples) {
        int m = samples.size();
        double score = 0;
        for(int i=0; i < m; ++i){
            score += bettermodel.getProbabilityDensity(samples.get(i));
        }
        return score / m;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        return 0;
    }
}
