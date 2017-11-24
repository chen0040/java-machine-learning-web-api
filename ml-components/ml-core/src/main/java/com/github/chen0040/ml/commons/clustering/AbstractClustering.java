package com.github.chen0040.ml.commons.clustering;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 */
public abstract class AbstractClustering extends AbstractMLModule implements Clustering{
    protected BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;

    public double computeDBI(IntelliContext batch){
        HashMap<String, ArrayList<IntelliTuple>> clusters = new HashMap<String, ArrayList<IntelliTuple>>();
        int m = batch.tupleCount();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String predictedLabel = tuple.getPredictedLabelOutput();
            if(clusters.containsKey(predictedLabel)){
                clusters.get(predictedLabel).add(tuple);
            }else{
                ArrayList<IntelliTuple> cluster = new ArrayList<IntelliTuple>();
                cluster.add(tuple);
                clusters.put(predictedLabel, cluster);
            }
        }
        HashMap<String, Double> clusterDistance = new HashMap<String, Double>();
        HashMap<String, IntelliTuple> clusterCenters = new HashMap<String, IntelliTuple>();

        ArrayList<String> labels = new ArrayList<>();
        for(String label : clusters.keySet()){
            ArrayList<IntelliTuple> points = clusters.get(label);
            IntelliTuple center = getCenter(batch, points);
            clusterCenters.put(label, center);
            clusterDistance.put(label, getInnerDistance(batch, points, center));
            labels.add(label);
        }

        if(labels.isEmpty()) return 0;

        double DB = Double.NEGATIVE_INFINITY;
        for(int i=0; i < labels.size(); ++i){
            String label_i = labels.get(i);
            double sigma_i = clusterDistance.get(label_i);
            IntelliTuple center_i = clusterCenters.get(label_i);
            for(int j=i+1; j < labels.size(); ++j){
                String label_j = labels.get(j);

                double sigma_j = clusterDistance.get(label_j);
                IntelliTuple center_j = clusterCenters.get(label_j);
                double inter_cluster_distance = DistanceMeasureService.getDistance(batch, center_i, center_j, distanceMeasure);
                double C = (sigma_i + sigma_j) / inter_cluster_distance;
                DB = Math.max(C, DB);
            }
        }

        DB /= labels.size();

        return DB;

    }

    public double getInnerDistance(IntelliContext batch, ArrayList<IntelliTuple> points, IntelliTuple center){
        double distanceSum = 0;
        for(int i=0; i < points.size(); ++i){
            double distance = DistanceMeasureService.getDistance(batch, points.get(i), center, distanceMeasure);
            distanceSum += distance;
        }
        return distanceSum / points.size();
    }

    public int getClusterCount(IntelliContext batch){
        int m = batch.tupleCount();
        HashSet<String> clusters = new HashSet<>();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String label = tuple.getPredictedLabelOutput();
            clusters.add(label);
        }
        return clusters.size();
    }

    private IntelliTuple getCenter(IntelliContext mgr, ArrayList<IntelliTuple> points){
        IntelliTuple center = mgr.newTuple();

        int length = points.get(0).tupleLength();
        for(int i=0; i < points.size(); ++i){
            IntelliTuple point = points.get(i);
            for(int j=0; j < length; ++j){
                if(mgr.isCategorical(j)){
                    center.set(j, (double) mgr.getAttributeValueAsLevel(point, j).getLevelIndex());
                }else{
                    center.set(j, mgr.getAttributeValueAsDouble(point, j, 0));
                }
            }
        }

        for(int i=0; i < length; ++i){
            center.set(i, mgr.getAttributeValueAsDouble(center, i, 0) / points.size());
        }

        return center;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        AbstractClustering rhs2 = (AbstractClustering)rhs;
        distanceMeasure = rhs2.distanceMeasure;
    }

    public abstract int getCluster(IntelliTuple tuple) ;

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }


    protected void updateClusterInfo(IntelliContext batch){
        int m = batch.tupleCount();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            int clusterIndex = getCluster(tuple);
            tuple.setPredictedLabelOutput(String.format("%d", clusterIndex));
        }
    }

    public int getClusterOfClosestTuple(IntelliTuple tuple, IntelliContext batch){
        int m = batch.tupleCount();
        double min_distance = Double.MAX_VALUE;
        IntelliTuple closest_tuple = null;
        for(int i=0; i < m; ++i){
            IntelliTuple known_tuple = batch.tupleAtIndex(i);
            double distance = DistanceMeasureService.getDistance(batch, tuple, known_tuple, distanceMeasure);
            if(distance < min_distance){
                min_distance = distance;
                closest_tuple = known_tuple;
            }
        }

        return Integer.parseInt(closest_tuple.getPredictedLabelOutput());
    }
}
