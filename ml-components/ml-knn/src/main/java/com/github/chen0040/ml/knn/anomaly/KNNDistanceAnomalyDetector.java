package com.github.chen0040.ml.knn.anomaly;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class KNNDistanceAnomalyDetector extends AbstractAnomalyDetecter {
    private StrategyType strategy;
    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private double DDistance;
    private int k;
    private int anomalyCount = 10;

    public KNNDistanceAnomalyDetector() {
        super();
        strategy = StrategyType.DDistance;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        KNNDistanceAnomalyDetector rhs2 = (KNNDistanceAnomalyDetector)rhs;
        k = rhs2.k;
        DDistance = rhs2.DDistance;
        anomalyCount = rhs2.anomalyCount;
        distanceMeasure = rhs2.distanceMeasure;
        strategy = rhs2.strategy;
    }

    @Override
    public Object clone(){
        KNNDistanceAnomalyDetector clone = new KNNDistanceAnomalyDetector();
        clone.copy(this);

        return clone;
    }

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public double getDDistance() {
        return DDistance;
    }

    public void setDDistance(double DDistance) {
        this.DDistance = DDistance;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    private void switch2DDistance(int k, double D){
        strategy = StrategyType.DDistance;
        this.k = k;
        DDistance = D;
    }

    public void switch2kthDistance(int k, int anomalyCount){
        strategy = StrategyType.kthNNDistance;
        this.k = k;
        this.anomalyCount = anomalyCount;
    }

    public void switch2AvgDistance(int k, int anomalyCount){
        strategy = StrategyType.AvgDistance;
        this.k = k;
        this.anomalyCount = anomalyCount;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        if (tuple.hasLabelOutput()) {
            return AnomalyClassLabels.IS_ANOMALY.equals(tuple.getPredictedLabelOutput());
        }
        return false;
    }

    public boolean checkAnomaly(IntelliContext batch, IntelliTuple tuple) {

        int neighborSize = 0;
        for (int i = 0; i < batch.tupleCount(); ++i) {
            IntelliTuple ti = batch.tupleAtIndex(i);
            if (ti == tuple) continue;
            double distance = DistanceMeasureService.getDistance(batch, ti, tuple, distanceMeasure);
            if (distance < k) {
                neighborSize++;
            }
            if (neighborSize >= k) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);

        switch (strategy) {

            case DDistance:
                return doDDistance(batch);
            case kthNNDistance:
                return doKthDistance(batch);
            case AvgDistance:
                return doAvgDistance(batch);
            case DensityBased:
                return doDensityBased(batch);
        }

        return new BatchUpdateResult(new Exception(String.format("Unknown Strategy Type \"%s\"", strategy)));

    }

    private BatchUpdateResult doDDistance(IntelliContext batch) {
        for (int i = 0; i < batch.tupleCount(); ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            boolean isAnomaly = checkAnomaly(batch, tuple);
            tuple.setPredictedLabelOutput(isAnomaly ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY);
        }

        return new BatchUpdateResult();
    }

    public BatchUpdateResult doDensityBased(IntelliContext batch){
        return new BatchUpdateResult();
    }

    private BatchUpdateResult doKthDistance(IntelliContext batch) {
        HashMap<IntelliTuple, Double> anomalyCandidates = new HashMap<IntelliTuple, Double>();
        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            tuple.setPredictedLabelOutput(AnomalyClassLabels.IS_NOT_ANOMALY);

            Object[] result = DistanceMeasureService.getKthNearestNeighbor(batch, tuple, k, distanceMeasure);
            IntelliTuple kthNeighbor = (IntelliTuple)result[0];
            double distance = (Double)result[1];
            if(anomalyCandidates.size() < anomalyCount){
                anomalyCandidates.put(tuple, distance);
            }else{
                double max_kth_distance = Double.MIN_VALUE;
                IntelliTuple candidate_with_max_kth_distance = null;
                for(IntelliTuple ti : anomalyCandidates.keySet()){
                    double kth_distance = anomalyCandidates.get(ti);
                    if(kth_distance > max_kth_distance){
                        max_kth_distance  = kth_distance;
                        candidate_with_max_kth_distance = ti;
                    }
                }
                if(max_kth_distance > distance){
                    anomalyCandidates.remove(candidate_with_max_kth_distance);
                    anomalyCandidates.put(tuple, distance);
                }
            }
        }

        for(IntelliTuple ti : anomalyCandidates.keySet()){
            ti.setPredictedLabelOutput(AnomalyClassLabels.IS_ANOMALY);
        }

        return new BatchUpdateResult();

    }

    private BatchUpdateResult doAvgDistance(IntelliContext batch) {
        HashMap<IntelliTuple, Double> anomalyCandidates = new HashMap<IntelliTuple, Double>();

        for (int i = 0; i < batch.tupleCount(); ++i) {
            IntelliTuple tuple = batch.tupleAtIndex(i);
            tuple.setPredictedLabelOutput(AnomalyClassLabels.IS_NOT_ANOMALY);

            Map<IntelliTuple, Double> neighbors = DistanceMeasureService.getKNearestNeighbors(batch, tuple, k, distanceMeasure);

            double avgDistance = 0;
            for(Double distance : neighbors.values()){
                avgDistance+=distance;
            }
            avgDistance /= neighbors.size();
            if(anomalyCandidates.size() < anomalyCount){
                anomalyCandidates.put(tuple, avgDistance);
            }else{
                double max_avg_distance = Double.MIN_VALUE;
                IntelliTuple candidate_with_max_avg_distance = null;
                for(IntelliTuple ti : anomalyCandidates.keySet()){
                    double candidate_avg_distance = anomalyCandidates.get(ti);
                    if(candidate_avg_distance > max_avg_distance){
                        max_avg_distance  = candidate_avg_distance;
                        candidate_with_max_avg_distance = ti;
                    }
                }
                if(max_avg_distance > avgDistance){
                    anomalyCandidates.remove(candidate_with_max_avg_distance);
                    anomalyCandidates.put(tuple, avgDistance);
                }
            }
        }

        for(IntelliTuple ti : anomalyCandidates.keySet()){
            ti.setPredictedLabelOutput(AnomalyClassLabels.IS_ANOMALY);
        }

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tupe, IntelliContext context) {
        throw new NotImplementedException();
    }

    public enum StrategyType
    {
        DDistance,
        //D-Distance Anomalies are data points which have fewer
        //than p neighboring points within a distance D [22].

        kthNNDistance,
        //(ii) kth NN Distance Anomalies are the top-ranked instances
        //whose distance to the kth nearest neighbor is greatest

        AvgDistance,
        //(iii) Average kNN Distance Anomalies are the top-ranked
        //instances whose average distance to the k nearest neighbors
        //is greatest [4].

        DensityBased
        //(iv) Density-based Anomalies are instances which are in
        //regions of low density or low local/relative density. (Essentially LOF type)
    }
}
