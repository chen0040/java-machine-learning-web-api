package com.github.chen0040.ml.clustering.onelink;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class SingleLinkageClustering extends AbstractClustering {
    private int clusterCount = 10;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        SingleLinkageClustering rhs2 = (SingleLinkageClustering)rhs;
        clusterCount = rhs2.clusterCount;
    }

    @Override
    public Object clone(){
        SingleLinkageClustering clone = new SingleLinkageClustering();
        clone.copy(this);

        return clone;
    }

    public SingleLinkageClustering(){
        super();
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public void setClusterCount(int clusterCount) {
        this.clusterCount = clusterCount;
    }

    @Override
    public int getCluster(IntelliTuple tuple) {
        if(tuple.getTag() != null){
            Cluster cluster = (Cluster)tuple.getTag();
            return cluster.getIndex();
        }
        return -1;
    }

    private double getDistance(IntelliContext context, Cluster c1, Cluster c2){
        List<IntelliTuple> ct1 = c1.getTuples();
        List<IntelliTuple> ct2 = c2.getTuples();
        int m1 = ct1.size();
        int m2 = ct2.size();
        double min_distance = Double.MAX_VALUE;
        for(int i=0; i < m1; ++i){
            IntelliTuple tuple1 = ct1.get(i);
            for(int j=0; j < m2; ++j){
                IntelliTuple tuple2 = ct2.get(j);
                double distance = DistanceMeasureService.getDistance(context, tuple1, tuple2, distanceMeasure);
                min_distance = Math.min(min_distance, distance);
            }
        }

        return min_distance;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);

        int m = batch.tupleCount();
        Cluster[] clusters = new Cluster[m];
        for(int i = 0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);

            clusters[i] = new Cluster(tuple, i);
        }

        int remainingClusterCount = m;
        for(int i=0; i < (m-clusterCount); ++i){
            remainingClusterCount--;
            Cluster[] newClusters = new Cluster[remainingClusterCount];

            int select_j = -1;
            int select_k = -1;
            double min_distance = Double.MAX_VALUE;
            for(int j=0; j < clusters.length; ++j){
                Cluster cluster_j = clusters[j];
                for(int k=j+1; k < clusters.length; ++k){
                    Cluster cluster_k = clusters[k];
                    double distance = getDistance(batch, cluster_j, cluster_k);
                    if(distance < min_distance){
                        select_j = j;
                        select_k = k;
                        min_distance = distance;
                    }
                }


            }

            int newIndex = 0;
            for(int l=0; l < clusters.length; ++l){
                if(l != select_j && l != select_k){
                    newClusters[newIndex++] = clusters[l];
                }
            }

            clusters[select_j].add(clusters[select_k]);
            newClusters[newIndex] = clusters[select_j];

            clusters = newClusters;
        }

        for(int i=0; i < clusters.length; ++i){
            clusters[i].setIndex(i);
        }

        updateClusterInfo(batch);


        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        if(tuple.getTag() != null) {
            Cluster c = (Cluster)tuple.getTag();
            return c.getIndex();
        }
        return -1;
    }

    private class Cluster{
        private int index;
        private List<IntelliTuple> tuples;

        public Cluster(IntelliTuple tuple, int index){
            this.index = index;
            tuples = new ArrayList<IntelliTuple>();
            add(tuple);
        }

        public int getIndex(){
            return index;
        }

        public void setIndex(int index){
            this.index = index;
        }

        @Override
        public int hashCode(){
            return this.index;
        }

        @Override
        public boolean equals(Object rhs){
            if(rhs instanceof Cluster){
                Cluster cast_rhs = (Cluster) rhs;
                return cast_rhs.index == index;
            }
            return false;
        }

        public void add(Cluster cluster){
            for(IntelliTuple tuple : cluster.getTuples()){
                add(tuple);
            }
            cluster.getTuples().clear();
        }

        private void add(IntelliTuple tuple){
            this.tuples.add(tuple);
            tuple.setTag(this);
        }

        public List<IntelliTuple> getTuples(){
            return tuples;
        }
    }
}
