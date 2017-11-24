package com.github.chen0040.ml.spark.clustering;

import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.core.clustering.AbstractClustering;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class KMeans extends AbstractClustering {
    private static final Random random = new Random();
    private List<SparkMLTuple> clusters = new ArrayList<SparkMLTuple>();
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
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        KMeans rhs2 = (KMeans)rhs;
        clusters = new ArrayList<SparkMLTuple>();

        for(int i=0; i < rhs2.clusters.size(); ++i){
            clusters.set(i, (SparkMLTuple)rhs2.clusters.get(i).clone());
        }
    }


    public KMeans(){
        setAttribute(K, 5);
        setAttribute(MAX_ITERS, 2000);
    }


    public List<SparkMLTuple> getClusters() {
        return clusters;
    }

    @Override
    public int getCluster(JavaSparkContext context, SparkMLTuple tuple)
    {
        double minDistance = Double.MAX_VALUE;
        int closestClusterIndex = -1;
        for (int j = 0; j < clusterCount(); ++j) {
            SparkMLTuple clusterCenter = clusters.get(j);
            double distance = DistanceMeasureService.getDistance(tuple, clusterCenter, distanceMeasure);
            if (minDistance > distance) {
                minDistance = distance;
                closestClusterIndex = j;
            }
        }
        return closestClusterIndex;
    }

    private void initializeCluster(JavaRDD<SparkMLTuple> batch){
        if(clusters.size() != clusterCount()) {
            List<SparkMLTuple> samples = batch.takeSample(false, clusterCount());
            clusters.clear();
            clusters.addAll(samples);
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {

        initializeCluster(batch);

        for(int iter = 0; iter < maxIters(); ++iter) {


            // do clustering
            JavaPairRDD<Integer, Tuple2<SparkMLTuple, Integer>> cluster_rdd = batch.mapToPair(new PairFunction<SparkMLTuple, Integer, Tuple2<SparkMLTuple, Integer>>() {
                public Tuple2<Integer, Tuple2<SparkMLTuple, Integer>> call(SparkMLTuple point) throws Exception {
                    double minDistance = Double.MAX_VALUE;
                    int closestClusterIndex = -1;

                    for (int j = 0; j < clusterCount(); ++j) {
                        SparkMLTuple clusterCenter = clusters.get(j);
                        double distance = DistanceMeasureService.getDistance(point, clusterCenter, distanceMeasure);
                        if (minDistance > distance) {
                            minDistance = distance;
                            closestClusterIndex = j;
                        }
                    }
                    return new Tuple2<Integer, Tuple2<SparkMLTuple, Integer>>(closestClusterIndex, new Tuple2<SparkMLTuple, Integer>(point, 1));
                }
            });

            //readjust cluster center
            JavaPairRDD<Integer, Tuple2<SparkMLTuple, Integer>> group_rdd = cluster_rdd.reduceByKey(new Function2<Tuple2<SparkMLTuple, Integer>, Tuple2<SparkMLTuple, Integer>, Tuple2<SparkMLTuple, Integer>>() {
                public Tuple2<SparkMLTuple, Integer> call(Tuple2<SparkMLTuple, Integer> t1, Tuple2<SparkMLTuple, Integer> t2) throws Exception {
                    SparkMLTuple p1 = t1._1();
                    SparkMLTuple p2 = t2._1();

                    int i1 = t1._2();
                    int i2 = t2._2();

                    SparkMLTuple p = p2.clone();
                    int dimension = p1.getDimension();
                    for(int i=0; i < dimension; ++i){
                        if(p.containsKey(i) || p2.containsKey(i)){
                            p.put(i, p.getOrDefault(i, 0.0) + p2.getOrDefault(i, 0.0));
                        }
                    }

                    return new Tuple2<SparkMLTuple, Integer>(p, i1 + i2);
                }
            });

            List<Tuple2<Integer, Tuple2<SparkMLTuple, Integer>>> new_cluster_centers = group_rdd.collect();

            for(int i=0; i < new_cluster_centers.size(); ++i){
                Tuple2<Integer, Tuple2<SparkMLTuple, Integer>> new_cluster_center = new_cluster_centers.get(i);
                int clusterIndex = new_cluster_center._1();
                Tuple2<SparkMLTuple, Integer> new_cluster_info = new_cluster_center._2();
                SparkMLTuple new_cluster_center_point = new_cluster_info._1();
                int num_points_in_new_cluster = new_cluster_info._2();
                for(Integer featureIndex : new_cluster_center_point.keySet()){
                    new_cluster_center_point.put(featureIndex, new_cluster_center_point.get(featureIndex) / num_points_in_new_cluster);
                }
                clusters.set(clusterIndex, new_cluster_center_point);
            }
        }

        return new BatchUpdateResult();


    }

    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        throw new NotImplementedException();
    }


}
