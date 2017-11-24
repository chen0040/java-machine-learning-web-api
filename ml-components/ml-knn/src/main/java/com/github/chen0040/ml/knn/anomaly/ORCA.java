package com.github.chen0040.ml.knn.anomaly;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 * Link:
 * http://stephenbay.net/papers/outliers.kdd03.pdf
 */
public class ORCA extends AbstractAnomalyDetecter{
    private int numBlocks = 20;
    private int k = 5;
    private int anomalyCount = 10;
    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private BiFunction<IntelliTuple, Map<IntelliTuple, Double>, Double> scoreFunction;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        ORCA rhs2 = (ORCA)rhs;
        numBlocks = rhs2.numBlocks;
        k = rhs2.k;
        anomalyCount = rhs2.anomalyCount;
        distanceMeasure = rhs2.distanceMeasure;
        scoreFunction = rhs2.scoreFunction;
    }

    @Override
    public Object clone(){
        ORCA clone = new ORCA();
        clone.copy(this);
        return clone;
    }

    private static IntelliContext merge(IntelliContext batch1, IntelliContext batch2, int n){
        List<IntelliTuple> list = new ArrayList<IntelliTuple>();
        for(int i = 0; i < batch1.tupleCount(); ++i){
            list.add(batch1.tupleAtIndex(i));
        }
        for(int i = 0; i < batch2.tupleCount(); ++i){
            list.add(batch2.tupleAtIndex(i));
        }

        // sort descendingly based on their scores
        Collections.sort(list, new Comparator<IntelliTuple>() {
            public int compare(IntelliTuple o1, IntelliTuple o2) {
                double score1 = (Double)o1.getTag();
                double score2 = (Double)o2.getTag();

                return Double.compare(score2, score1);
            }
        });

        IntelliContext batch = new IntelliContext();
        batch.copyWithoutTuples(batch1);
        for(int i=0; i < n && i < list.size(); ++i){
            batch.add(list.get(i));
        }
        return batch;
    }

    public BiFunction<IntelliTuple, Map<IntelliTuple, Double>, Double> getScoreFunction() {
        return scoreFunction;
    }

    public void setScoreFunction(BiFunction<IntelliTuple, Map<IntelliTuple, Double>, Double> scoreFunction) {
        this.scoreFunction = scoreFunction;
    }

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        return AnomalyClassLabels.IS_ANOMALY.equals(tuple.getPredictedLabelOutput());
    }

    private double distance(IntelliContext context, IntelliTuple a, IntelliTuple b){
        return DistanceMeasureService.getDistance(context, a, b, distanceMeasure);
    }

    private double maxdist(Map<IntelliTuple, Double> neighbors){
        double largest_distance = Double.MIN_VALUE;
        for(IntelliTuple tj : neighbors.keySet()){
            double tj_distance = neighbors.get(tj);
            if(tj_distance > largest_distance){
                largest_distance =tj_distance;
            }
        }
        return largest_distance;
    }

    public void merge(IntelliContext context, IntelliTuple t, Map<IntelliTuple, Double> neighbors, IntelliTuple ti){
        double distance = DistanceMeasureService.getDistance(context, ti, t, distanceMeasure);
        if(neighbors.size() < k){
            neighbors.put(ti, distance);
        }else{
            double largest_distance = Double.MIN_VALUE;
            IntelliTuple neighbor_with_largest_distance = null;
            for(IntelliTuple tj : neighbors.keySet()){
                double tj_distance = neighbors.get(tj);
                if(tj_distance > largest_distance){
                    largest_distance =tj_distance;
                    neighbor_with_largest_distance = tj;
                }
            }

            if(largest_distance > distance){
                neighbors.remove(neighbor_with_largest_distance);
                neighbors.put(ti, distance);
            }
        }
    }

    private double score(IntelliTuple b, Map<IntelliTuple, Double> neighbors){
        if(scoreFunction==null){
            double avg_distance = 0;
            for(Double distance : neighbors.values()){
                avg_distance += distance;
            }
            return avg_distance / neighbors.size();
        }
        else{
            return scoreFunction.apply(b, neighbors);
        }
    }

    private double minScore(IntelliContext batch){
        double min_score = Double.MAX_VALUE;
        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double score = (Double)tuple.getTag();
            min_score = Math.min(min_score, score);
        }

        return min_score;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);

        double cutoff = 0;
        IntelliContext anomalies = new IntelliContext();
        anomalies.copyWithoutTuples(batch);

        int m = batch.tupleCount();
        int blockSize = m / numBlocks;
        for(int i=0; i < numBlocks; ++i){
            IntelliContext block = getBlock(batch, i, blockSize);


            Map<IntelliTuple, Map<IntelliTuple, Double>> b_neighbors = new HashMap<IntelliTuple, Map<IntelliTuple, Double>>();
            for(int k = 0; k < block.tupleCount(); ++k){
                b_neighbors.put(block.tupleAtIndex(k), new HashMap<IntelliTuple, Double>());
            }

            for(int j=0; j < m; ++j){
                IntelliTuple d = batch.tupleAtIndex(j);

                int block_size = block.tupleCount();

                IntelliContext tempBatch = new IntelliContext();
                for(int l = 0; l < block_size; ++l){
                    IntelliTuple b = block.tupleAtIndex(l);
                    if(b == d){
                        tempBatch.add(b);
                        continue;
                    }

                    Map<IntelliTuple, Double> neighbors = b_neighbors.get(b);

                    boolean shouldInclude = true;
                    if(neighbors.size() < k || distance(batch, b, d) < maxdist(neighbors)){
                        merge(batch, b, neighbors, d);

                        double score = score(b, neighbors);
                        b.setTag(new Double(score));
                        if(score < cutoff){
                            shouldInclude = false;
                        }
                    }

                    if(shouldInclude){
                        tempBatch.add(b);
                    }

                }
                block = tempBatch;
            }

            //System.out.println("block size: "+block.size());

            anomalies = merge(anomalies, block, anomalyCount);

            //System.out.println("anomaly size: "+anomalies.size());
            cutoff = minScore(anomalies);
        }

        for(int i = 0; i < batch.tupleCount(); ++i){
            batch.tupleAtIndex(i).setPredictedLabelOutput(AnomalyClassLabels.IS_NOT_ANOMALY);
        }

        for(int i = 0; i < anomalies.tupleCount(); ++i){
            anomalies.tupleAtIndex(i).setPredictedLabelOutput(AnomalyClassLabels.IS_ANOMALY);
        }

        return new BatchUpdateResult();
    }

    private IntelliContext getBlock(IntelliContext batch, int blockIndex, int blockSize){
        int batchStartIndex = blockIndex * blockSize;
        int batchEndIndex = batchStartIndex + blockSize;
        IntelliContext block= new IntelliContext();
        block.copyWithoutTuples(batch);
        for(int i = batchStartIndex; i < batchEndIndex && i < batch.tupleCount(); ++i){
            block.add(batch.tupleAtIndex(i));
        }
        return block;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        Double value = (Double)tuple.getTag();
        if(value != null){
            return value;
        }else{
            return -1;
        }

    }
}
