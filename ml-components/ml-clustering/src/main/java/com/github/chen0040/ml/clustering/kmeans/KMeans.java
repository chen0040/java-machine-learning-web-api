package com.github.chen0040.ml.clustering.kmeans;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.clustering.AbstractClustering;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import com.github.chen0040.ml.commons.IntelliTuple;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class KMeans extends AbstractClustering {
    private static final Random random = new Random();
    private IntelliTuple[] clusters;
    public static final String MAX_ITERS = "Maximum Iterations";
    public static final String K = "K (Cluster Count)";

    private int maxIters(){
        return (int)getAttribute(MAX_ITERS);
    }

    private int clusterCount(){
        return (int)getAttribute(K);
    }

    private void _clusterCount(int k){
        setAttribute(K, k);
    }

    @Override
    public Object clone(){
        KMeans clone = new KMeans();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        KMeans rhs2 = (KMeans)rhs;
        clusters = null;
        if(rhs2.clusters != null){
            clusters = new IntelliTuple[rhs2.clusters.length];
            for(int i=0; i < rhs2.clusters.length; ++i){
                clusters[i] = (IntelliTuple)rhs2.clusters[i].clone();
            }
        }
    }


    public KMeans(){
        setAttribute(K, 5);
        setAttribute(MAX_ITERS, 2000);
    }


    public IntelliTuple[] getClusters() {
        return clusters;
    }

    @Override
    public int getCluster(IntelliTuple tuple)
    {
        double minDistance = Double.MAX_VALUE;
        int closestClusterIndex = -1;
        IntelliContext context = getModelSource();
        for (int j = 0; j < clusterCount(); ++j) {
            IntelliTuple clusterCenter = clusters[j];
            double distance = DistanceMeasureService.getDistance(context, tuple, clusterCenter, distanceMeasure);
            if (minDistance > distance) {
                minDistance = distance;
                closestClusterIndex = j;
            }
        }
        return closestClusterIndex;
    }

    private void initializeCluster(IntelliContext batch){
        if(clusters == null || clusters.length != clusterCount()) {
            HashSet<Integer> indexList = new HashSet<Integer>();
            int m = batch.tupleCount();
            if (m < clusterCount() * 3) {
                _clusterCount(Math.min(m, clusterCount()));
                for (int i = 0; i < clusterCount(); ++i) {
                    indexList.add(i);
                }
            } else {

                while (indexList.size() < clusterCount()) {
                    int r = random.nextInt(m);
                    if (!indexList.contains(r)) {
                        indexList.add(r);
                    }
                }
            }

            clusters = new IntelliTuple[clusterCount()];
            for (int i = 0; i < clusterCount(); ++i) {
                clusters[i] = batch.newTuple();
            }

            // initialize cluster centers
            int clusterIndex = 0;
            for (Integer i : indexList) {
                IntelliTuple center = clusters[clusterIndex];
                IntelliTuple t = batch.tupleAtIndex(i);
                int n = t.tupleLength();
                for (int j = 0; j < n; ++j) {
                    center.set(j, batch.getAttributeValueAsDouble(t, j, 0));
                }
                clusterIndex++;
            }
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);

        initializeCluster(batch);
        int m = batch.tupleCount();

        for(int iter = 0; iter < maxIters(); ++iter) {
            Cluster[] cg = new Cluster[clusterCount()];
            for (int i = 0; i < clusterCount(); ++i) {
                cg[i] = new Cluster();
            }

            // do clustering
            for (int i = 0; i < m; ++i) {
                IntelliTuple tuple = batch.tupleAtIndex(i);

                double minDistance = Double.MAX_VALUE;
                int closestClusterIndex = -1;
                for (int j = 0; j < clusterCount(); ++j) {
                    IntelliTuple clusterCenter = clusters[j];
                    double distance = DistanceMeasureService.getDistance(batch, tuple, clusterCenter, distanceMeasure);
                    if (minDistance > distance) {
                        minDistance = distance;
                        closestClusterIndex = j;
                    }
                }

                cg[closestClusterIndex].append(tuple);
                tuple.setPredictedLabelOutput(String.format("%d", closestClusterIndex));
            }

            //readjust cluster center
            for(int i=0; i < clusterCount(); ++i){
                IntelliTuple newCenter = cg[i].calcCenter(batch);
                if(newCenter != null){
                    clusters[i]=newCenter;
                }
            }

        }

        updateClusterInfo(batch);



        return new BatchUpdateResult();


    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }

    private class Cluster{
        private ArrayList<IntelliTuple> elements;
        public Cluster(){
            elements = new ArrayList<IntelliTuple>();
        }

        public void append(IntelliTuple tuple){
            elements.add(tuple);
        }

        public IntelliTuple calcCenter(IntelliContext context){
            if(elements.isEmpty()) return null;
            IntelliTuple newCenter = context.newTuple();
            int n = elements.get(0).tupleLength();
            int m = elements.size();
            for(int i=0; i < n; ++i){
                double sum = 0;
                for(int j=0; j < m; ++j){
                    sum += context.getAttributeValueAsDouble(elements.get(j), i, 0);
                }

                newCenter.set(i, sum / m);
            }
            return newCenter;
        }
    }
}
