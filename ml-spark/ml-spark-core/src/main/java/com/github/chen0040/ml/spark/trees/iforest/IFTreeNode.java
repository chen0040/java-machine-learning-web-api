package com.github.chen0040.ml.spark.trees.iforest;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLStatistics;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class IFTreeNode implements Serializable{
    private long tupleCount;
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

    public IFTreeNode(JavaRDD<SparkMLTuple> batch, Random random, int height, int maxHeight){
        tupleCount = batch.count();
        nodeId = UUID.randomUUID().toString();

        if(tupleCount <= 1 || height == maxHeight){
            return;
        }

        double[] minValues = SparkMLStatistics.minByFeature(batch);
        double[] maxValues = SparkMLStatistics.maxByFeature(batch);

        int n = minValues.length;

        List<Integer> featureList = new ArrayList<Integer>();
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

            JavaRDD<SparkMLTuple> batch1 = batch.filter(new Function<SparkMLTuple, Boolean>() {
                public Boolean call(SparkMLTuple t) throws Exception {
                    return (t.get(featureIndex) < splitPoint);
                }
            });
            JavaRDD<SparkMLTuple> batch2 = batch.filter(new Function<SparkMLTuple, Boolean>() {
                public Boolean call(SparkMLTuple t) throws Exception {
                    return (t.get(featureIndex) >= splitPoint);
                }
            });

            childNodes = new ArrayList<IFTreeNode>();

            childNodes.add(new IFTreeNode(batch1, random, height+1, maxHeight));
            childNodes.add(new IFTreeNode(batch2, random, height+1, maxHeight));

        }
    }

    public static double heuristicCost(double n){
        if(n <= 1.0) return 0;
        return 2 * (Math.log(n - 1) + 0.5772156649) - (2 * (n - 1) / n);
    }

    public long getTupleCount() {
        return tupleCount;
    }

    public void setTupleCount(long tupleCount) {
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

    protected double pathLength(SparkMLTuple tuple){
        if(childNodes==null){
            return heuristicCost(tupleCount);
        }else{
            double featureValue = tuple.get(featureIndex);
            if(featureValue < splitPoint){
                return childNodes.get(0).pathLength(tuple)+1.0;
            }else{
                return childNodes.get(1).pathLength(tuple)+1.0;
            }
        }
    }
}
