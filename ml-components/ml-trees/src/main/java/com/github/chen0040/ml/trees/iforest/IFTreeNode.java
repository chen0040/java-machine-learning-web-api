package com.github.chen0040.ml.trees.iforest;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.IntelliContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class IFTreeNode {
    private int tupleCount;
    private int featureIndex;
    private double splitPoint;
    private ArrayList<IFTreeNode> childNodes;
    private String nodeId;

    public void copy(IFTreeNode rhs){
        tupleCount = rhs.tupleCount;
        featureIndex = rhs.featureIndex;
        splitPoint = rhs.splitPoint;
        nodeId = rhs.nodeId;

        childNodes =  null;
        if(rhs.childNodes != null){
            childNodes = new ArrayList<IFTreeNode>();
            for(int i=0; i < rhs.childNodes.size(); ++i){
                childNodes.add((IFTreeNode)rhs.childNodes.get(i).clone());
            }
        }
    }

    @Override
    public Object clone(){
        IFTreeNode clone = new IFTreeNode();
        clone.copy(this);
        
        return clone;
    }

    public IFTreeNode(){
        childNodes = new ArrayList<IFTreeNode>();
        nodeId = UUID.randomUUID().toString();
    }

    private static double epsilon(){
        return 0.00000000001;
    }

    public IFTreeNode(IntelliContext batch, Random random, int height, int maxHeight){
        tupleCount = batch.tupleCount();
        nodeId = UUID.randomUUID().toString();

        if(tupleCount <= 1 || height == maxHeight){
            return;
        }

        int n = batch.toNumericArray(batch.tupleAtIndex(0)).length;

        double[] minValues = new double[n];
        double[] maxValues = new double[n];

        for(int i=0; i < tupleCount; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double[] x = batch.toNumericArray(tuple);
            int n_min = Math.min(n, x.length);
            for(int j=0; j < n_min; ++j){
                minValues[j] = Math.min(minValues[j], x[j]);
                maxValues[j] = Math.max(maxValues[j], x[j]);
            }
        }

        List<Integer> featureList = new ArrayList<>();
        for(int i=0; i < n; ++i){
            if(minValues[i] < maxValues[i] && (maxValues[i] - minValues[i]) > epsilon()){
                featureList.add(i);
            }
        }

        if(featureList.isEmpty()){
            return;
        }else{
            featureIndex =  random.nextInt(featureList.size());
            splitPoint = minValues[featureIndex] + (maxValues[featureIndex] - minValues[featureIndex]) * random.nextDouble();

            IntelliContext[] batches = new IntelliContext[2];
            childNodes = new ArrayList<>();

            for(int i=0; i < batches.length; ++i){
                batches[i]= new IntelliContext();
                batches[i].copyWithoutTuples(batch);
            }

            for(int i=0; i < tupleCount; ++i){
                IntelliTuple tuple = batch.tupleAtIndex(i);
                double[] x = batch.toNumericArray(tuple);
                double featureValue = x[featureIndex];
                if(featureValue < splitPoint){
                    batches[0].add(tuple);
                }else{
                    batches[1].add(tuple);
                }
            }

            for(int i=0; i < batches.length; ++i){
                childNodes.add(new IFTreeNode(batches[i], random, height+1, maxHeight));
            }

        }
    }

    public static double heuristicCost(double n){
        if(n <= 1.0) return 0;
        return 2 * (Math.log(n - 1) + 0.5772156649) - (2 * (n - 1) / n);
    }

    public int getTupleCount() {
        return tupleCount;
    }

    public void setTupleCount(int tupleCount) {
        this.tupleCount = tupleCount;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    public void setFeatureIndex(int featureIndex) {
        this.featureIndex = featureIndex;
    }

    public double getSplitPoint() {
        return splitPoint;
    }

    public void setSplitPoint(double splitPoint) {
        this.splitPoint = splitPoint;
    }

    public ArrayList<IFTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(ArrayList<IFTreeNode> childNodes) {
        this.childNodes = childNodes;
    }

    protected double pathLength(IntelliContext context, IntelliTuple tuple){
        if(childNodes==null){
            return heuristicCost(tupleCount);
        }else{
            double[] x = context.toNumericArray(tuple);
            double featureValue = x[featureIndex];
            if(featureValue < splitPoint){
                return childNodes.get(0).pathLength(context, tuple)+1.0;
            }else{
                return childNodes.get(1).pathLength(context, tuple)+1.0;
            }
        }
    }
}
