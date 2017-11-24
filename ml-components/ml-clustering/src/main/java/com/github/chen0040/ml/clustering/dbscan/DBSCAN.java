package com.github.chen0040.ml.clustering.dbscan;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 23/8/15.
 * Link:
 * Density-based spatial clustering of applications with noise
 * https://en.wikipedia.org/wiki/DBSCAN
 */
public class DBSCAN extends AbstractClustering {
    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private double epsilon;
    private int minPts;
    private IntelliContext model;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        DBSCAN rhs2 = (DBSCAN)rhs;
        epsilon = rhs2.epsilon;
        minPts = rhs2.minPts;
        distanceMeasure = rhs2.distanceMeasure;
        model = rhs2.model == null ? null : rhs2.model.clone();
    }

    @Override
    public Object clone(){
        DBSCAN clone = new DBSCAN();
        clone.copy(this);

        return clone;
    }

    public DBSCAN(){
        epsilon = 0.1;
        minPts = 10;
    }

    @Override
    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    @Override
    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public int getMinPts() {
        return minPts;
    }

    public void setMinPts(int minPts) {
        this.minPts = minPts;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public int getCluster(IntelliTuple tuple) {
        return getClusterOfClosestTuple(tuple, model);
    }

    public IntelliContext getModel(){
        return model;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        this.model = batch.clone();

        int m = model.tupleCount();

        boolean[] visited = new boolean[m];
        double[][] distanceMatrix = new double[m][];
        for(int i=0; i < m; ++i) {
            distanceMatrix[i] = new double[m];
            IntelliTuple tuple = model.tupleAtIndex(i);
            tuple.setTag(new Tag(tuple.getTag()));
        }

        for(int i=0; i < m; ++i){
            IntelliTuple tuple_i = model.tupleAtIndex(i);
            for(int j=i+1; j < m; ++j){
                IntelliTuple tuple_j = model.tupleAtIndex(j);
                double distance = DistanceMeasureService.getDistance(batch, tuple_i, tuple_j, distanceMeasure);
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
            }
        }

        int C = -1;

        for(int i=0; i < m; ++i){
            if(visited[i]){
                continue;
            }
            visited[i] = true;
            HashSet<Integer> neighbors = regionQuery(i, epsilon, distanceMatrix);
            if(neighbors.size() < minPts){
                // mark i as NOISE
            }else{
                C++;
                expandCluster(i, neighbors, C, epsilon, minPts, visited, distanceMatrix, model);
            }
        }

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = model.tupleAtIndex(i);
            int clusterId = get_cluster(tuple);
            tuple.setPredictedLabelOutput(String.format("%d", clusterId));
            tuple.setTag(((Tag)tuple.getTag()).origTag);
        }

        return new BatchUpdateResult();
    }

    private void add_to_cluster(int i, int clusterId, IntelliContext batch){
        IntelliTuple tuple = batch.tupleAtIndex(i);
        Tag tag = (Tag)tuple.getTag();
        tag.clusterId = clusterId;
    }

    private boolean is_member_of_a_cluster(int i, IntelliContext batch){
       return get_cluster(i, batch) != -1;
    }

    private int get_cluster(int i, IntelliContext batch){
        IntelliTuple tuple = batch.tupleAtIndex(i);
        return get_cluster(tuple);
    }

    private int get_cluster(IntelliTuple tuple){
        Tag tag = (Tag)tuple.getTag();
        return tag.clusterId;
    }

    private void expandCluster(int i, HashSet<Integer> neighbors_i, int clusterId, double eps, int MinPts, boolean[] visited, double[][] distanceMatrix, IntelliContext batch) {
        add_to_cluster(i, clusterId, batch);
        Iterator<Integer> piter = neighbors_i.iterator();
        while (piter.hasNext()) {
            int p = piter.next();
            if (!visited[p]) {
                visited[p] = true;
                HashSet<Integer> neighbor_pts_prime = regionQuery(p, eps, distanceMatrix);
                if (neighbor_pts_prime.size() >= MinPts) {
                    neighbors_i.addAll(neighbor_pts_prime);
                    piter = neighbors_i.iterator();
                }
            }
            if (!is_member_of_a_cluster(p, batch)) {
                add_to_cluster(p, clusterId, batch);
            }
        }
    }

    private HashSet<Integer> regionQuery(int i, double eps, double[][] distanceMatrix){
        int m = distanceMatrix.length;
        HashSet<Integer> neighbors = new HashSet<Integer>();
        for(int j = 0; j < m; ++j){
            if(i == j) continue;
            double distance = distanceMatrix[i][j];
            if(distance < eps){
                neighbors.add(j);
            }
        }

        return neighbors;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }

    private class Tag {
        public int clusterId;
        public Object origTag;

        public Tag(Object origTag){
            this.origTag = origTag;
            clusterId = -1;
        }
    }
}
