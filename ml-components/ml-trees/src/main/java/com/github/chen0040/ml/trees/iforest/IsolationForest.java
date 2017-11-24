package com.github.chen0040.ml.trees.iforest;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.BatchUpdateResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 * Link:
 * http://www.dtic.mil/dtic/tr/fulltext/u2/a512628.pdf
 */
public class IsolationForest extends AbstractAnomalyDetecter {

    public static final String THRESHOLD = "threshold";
    public static final String TREE_COUNT = "treeCount";
    private static final double log2 = Math.log(2);
    private ArrayList<IFTreeNode> trees;
    private int batchSize;
    private static Random random = new Random();

    @Override
    public void copy(MLModule rhs){
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
    public boolean isAnomaly(IntelliTuple tuple) {
        return evaluate(tuple, getModelSource()) > threshold();
    }

    public int getBatchSize(){
        return batchSize;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        trees =new ArrayList<IFTreeNode>();
        batchSize = batch.tupleCount();
        int maxHeight = (int)Math.ceil(log2(batchSize));
        for(int i=0; i < treeCount(); ++i){
            IntelliContext treeBatch = randomize(batch);
            IFTreeNode tree = new IFTreeNode(treeBatch, random, 0, maxHeight);
            trees.add(tree);
        }

        return new BatchUpdateResult();
    }

    private IntelliContext randomize(IntelliContext batch){
        IntelliContext treeBatch = new IntelliContext();
        treeBatch.copyWithoutTuples(batch);
        treeBatch.setAttributes(batch.getAttributes());

        List<IntelliTuple> list =new ArrayList<IntelliTuple>();
        for(int i = 0; i < batch.tupleCount(); ++i){
            list.add(batch.tupleAtIndex(i));
        }
        Collections.shuffle(list);
        for(int i=0; i < list.size(); ++i){
            treeBatch.add(list.get(i));
        }
        return treeBatch;
    }

    public double[] getDistributionScores(IntelliTuple tuple, IntelliContext batch){
        double[] scores = new double[2];
        scores[0] = evaluate(tuple, batch);
        scores[1] = 1- scores[0];

        return scores;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double avgPathLength = 0;
        for(int i=0; i < trees.size(); ++i){
            avgPathLength += trees.get(i).pathLength(context, tuple);
        }
        avgPathLength /= trees.size();

        return Math.pow(2, - avgPathLength / IFTreeNode.heuristicCost(batchSize));
    }
}
