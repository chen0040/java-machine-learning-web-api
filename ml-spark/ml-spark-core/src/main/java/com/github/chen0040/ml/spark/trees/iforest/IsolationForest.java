package com.github.chen0040.ml.spark.trees.iforest;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.PredictionResult;
import com.github.chen0040.ml.spark.core.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 * Link:
 * http://www.dtic.mil/dtic/tr/fulltext/u2/a512628.pdf
 */
public class IsolationForest extends AbstractAnomalyDetecter {

    public static final String THRESHOLD = "threshold";
    public static final String TREE_COUNT = "treeCount";
    public static final String SPLIT_PORTION = "splitPortion";
    public static final String USE_CACHE = "useCache";

    private static final double log2 = Math.log(2);
    private ArrayList<IFTreeNode> trees;
    private long batchSize;
    private Random random = new Random();

    private boolean useCache() {
        return getAttribute(USE_CACHE) > 0.5;
    }

    private void _useCache(boolean value){
        setAttribute(USE_CACHE, value ? 1 : 0);
    }



    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        IsolationForest rhs2 = (IsolationForest)rhs;
        batchSize = rhs2.batchSize;

        trees = null;
        if(rhs2.trees != null) {

            trees = new ArrayList<IFTreeNode>();

            for (int i = 0; i < rhs2.trees.size(); ++i) {
                trees.add((IFTreeNode)rhs2.trees.get(i).clone());
            }
        }
    }

    @Override
    public Object clone(){
        IsolationForest clone = new IsolationForest();
        clone.copy(this);

        return clone;
    }

    public IsolationForest(){
        super();
        setAttribute(THRESHOLD, 0.5);
        setAttribute(TREE_COUNT, 100);
        setAttribute(SPLIT_PORTION, 1);

        _useCache(true);
    }

    private static double log2(double n){
        return Math.log(n) / log2;
    }

    public double threshold() {
        return getAttribute(THRESHOLD);
    }

    public ArrayList<IFTreeNode> getModel(){
        return trees;
    }

    public void setModel(ArrayList<IFTreeNode> trees){
        this.trees = trees;
    }

    public int treeCount() { return (int)getAttribute(TREE_COUNT); }

    @Override
    public boolean isAnomaly(SparkMLTuple tuple) {
        return evaluate(tuple).getValue() > threshold();
    }

    public long getBatchSize(){
        return batchSize;
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {
        //this.setModelSourceId(batch.getId());
        boolean useCache = useCache();

        if(useCache){
            batch.cache();
        }

        trees =new ArrayList<IFTreeNode>();
        batchSize = batch.count();
        int maxHeight = (int)Math.ceil(log2(batchSize));

        int treeCount = treeCount();

        for(int i=0; i < treeCount; ++i){
            JavaRDD<SparkMLTuple> treeBatch = trySplit(batch);
            IFTreeNode tree = new IFTreeNode(treeBatch, random, 0, maxHeight);
            trees.add(tree);
        }

        return new BatchUpdateResult();
    }

    private JavaRDD<SparkMLTuple> trySplit(JavaRDD<SparkMLTuple> batch){
        double splitPortion = getAttribute(SPLIT_PORTION);
        if(splitPortion < 1 && splitPortion > 0){
            double[] weights = new double[2];
            weights[0] = splitPortion;
            weights[1] = 1 - splitPortion;
            JavaRDD<SparkMLTuple>[] batches = batch.randomSplit(weights);
            return batches[0];
        }
        return batch;
    }


    public double[] getDistributionScores(SparkMLTuple tuple, JavaRDD<SparkMLTuple> batch){
        double[] scores = new double[2];
        scores[0] = evaluate(tuple).getValue();
        scores[1] = 1- scores[0];

        return scores;
    }

    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        double avgPathLength = 0;
        for(int i=0; i < trees.size(); ++i){
            avgPathLength += trees.get(i).pathLength(tuple);
        }
        avgPathLength /= trees.size();

        double value = Math.pow(2, - avgPathLength / IFTreeNode.heuristicCost(batchSize));
        PredictionResult result = new PredictionResult();
        result.setValue(value);

        return result;
    }
}
