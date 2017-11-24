package com.github.chen0040.ml.spark.core.discrete;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class KMeansFilter extends AbstractDiscreteFilter {

    private static Random random = new Random();
    private int clusterCount;
    private double[] clusters;
    private int maxIters = 500;

    @Override
    public Object clone(){
        KMeansFilter clone = new KMeansFilter();
        clone.copy(this);
        return clone;
    }

    public void copy(KMeansFilter rhs)
    {
        clusterCount = rhs.clusterCount;
        clusters = rhs.clusters == null ? null : rhs.clusters.clone();
        maxIters = rhs.maxIters;
    }

    public KMeansFilter(int index, int k) {
        super(index);
        clusterCount = k;
    }

    public KMeansFilter(){
        super(0);
        clusterCount = 10;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public void setClusterCount(int clusterCount) {
        this.clusterCount = clusterCount;
    }

    public int getMaxIters() {
        return maxIters;
    }

    public void setMaxIters(int maxIters) {
        this.maxIters = maxIters;
    }

    @Override
    public void build(JavaRDD<Double> batch) {

        int m = (int)batch.count();

        HashSet<Integer> initialCenters = new HashSet<Integer>();

        if(clusterCount * 3 > m) {
            clusterCount = Math.min(clusterCount, m);
        }

        clusters = new double[clusterCount];

        List<Double> sample = batch.takeSample(false, clusterCount);
        for(int i=0; i < sample.size(); ++i){
            clusters[i] = sample.get(i);
        }

        JavaPairRDD<Integer, Tuple2<Double, Integer>> rdd2 = batch.mapToPair(new PairFunction<Double, Integer, Tuple2<Double, Integer>>() {
            public Tuple2<Integer, Tuple2<Double, Integer>> call(Double point) throws Exception {
                int clusterIndex = closestClusterIndex(point);
                return new Tuple2<Integer, Tuple2<Double, Integer>>(clusterIndex, new Tuple2<Double, Integer>(point, 1));
            }
        });

        JavaPairRDD<Integer, Tuple2<Double, Integer>> rdd3 = rdd2.reduceByKey(new Function2<Tuple2<Double, Integer>, Tuple2<Double, Integer>, Tuple2<Double, Integer>>() {
            public Tuple2<Double, Integer> call(Tuple2<Double, Integer> s1, Tuple2<Double, Integer> s2) throws Exception {
                double p1 = s1._1;
                double p2 = s2._1;
                return new Tuple2<Double, Integer>(p1 + p2, s1._2 + s2._2);
            }
        });

        JavaPairRDD<Integer, Double> rdd4 = rdd3.mapValues(new Function<Tuple2<Double, Integer>, Double>() {
            public Double call(Tuple2<Double, Integer> s) throws Exception {
                return s._1 / s._2;
            }
        });

        Map<Integer, Double> new_centers = rdd4.collectAsMap();

        for(int clusterIndex : new_centers.keySet()){
            clusters[clusterIndex] = new_centers.get(clusterIndex);
        }
    }

    @Override
    public int discretize(double value) {
        return closestClusterIndex(value);
    }

    private int closestClusterIndex(double value){
        double min_distance = Double.MAX_VALUE;
        int closest_cluster_index = -1;
        double distance;
        for(int i=0; i < clusters.length; ++i){
            distance = (clusters[i] - value) * (clusters[i] - value);
            if(distance < min_distance){
                min_distance = distance;
                closest_cluster_index = i;
            }
        }
        return closest_cluster_index;
    }
}
