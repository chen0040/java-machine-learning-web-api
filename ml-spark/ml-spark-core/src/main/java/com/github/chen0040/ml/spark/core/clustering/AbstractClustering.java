package com.github.chen0040.ml.spark.core.clustering;


import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.core.*;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 */
public abstract class AbstractClustering extends AbstractSparkMLModule implements Clustering {
    protected BiFunction<SparkMLTuple, SparkMLTuple, Double> distanceMeasure;

    public int getCluster(JavaSparkContext context, SparkMLTuple tuple){
        throw new NotImplementedException();
    }

    public double computeDBI(JavaSparkContext context, JavaRDD<SparkMLTuple> batch){


        JavaPairRDD<String, Tuple2<SparkMLTuple, Integer>> rdd = batch.mapToPair(new PairFunction<SparkMLTuple, String, Tuple2<SparkMLTuple, Integer>>() {
            public Tuple2<String, Tuple2<SparkMLTuple, Integer>> call(SparkMLTuple t) throws Exception {
                return new Tuple2<String, Tuple2<SparkMLTuple, Integer>>(t.getPredictedLabelOutput(), new Tuple2<SparkMLTuple, Integer>(t, 1));
            }
        });

        JavaPairRDD<String, Tuple2<SparkMLTuple, Integer>> rdd2 = rdd.reduceByKey(new Function2<Tuple2<SparkMLTuple, Integer>, Tuple2<SparkMLTuple, Integer>, Tuple2<SparkMLTuple, Integer>>() {
            public Tuple2<SparkMLTuple, Integer> call(Tuple2<SparkMLTuple, Integer> t1, Tuple2<SparkMLTuple, Integer> t2) throws Exception {
                SparkMLTuple p1 = t1._1;
                SparkMLTuple p2 = t2._1;

                int i1 = t1._2;
                int i2 = t2._2;

                SparkMLTuple p = (SparkMLTuple) p2.clone();
                int dimension = p1.getDimension();
                for (int i = 0; i < dimension; ++i) {
                    if (p.containsKey(i) || p1.containsKey(i)) {
                        p.put(i, p.getOrDefault(i, 0.0) + p1.getOrDefault(i, 0.0));
                    }
                }
                return new Tuple2<SparkMLTuple, Integer>(p, i1 + i2);
            }
        });

        JavaPairRDD<String, SparkMLTuple> rdd3 = rdd2.mapValues(new Function<Tuple2<SparkMLTuple, Integer>, SparkMLTuple>() {
            public SparkMLTuple call(Tuple2<SparkMLTuple, Integer> t) throws Exception {
                SparkMLTuple p = t._1;
                for (Integer index : p.keySet()) {
                    p.put(index, p.get(index) / t._2);
                }
                return p;
            }
        });

        final Map<String, SparkMLTuple> clusterCenters = rdd3.collectAsMap();

        if(clusterCenters.size()==0) return 0;

        JavaPairRDD<String, Tuple2<Double, Integer>> rdd4 = rdd.mapToPair(new PairFunction<Tuple2<String, Tuple2<SparkMLTuple, Integer>>, String, Tuple2<Double, Integer>>() {
            public Tuple2<String, Tuple2<Double, Integer>> call(Tuple2<String, Tuple2<SparkMLTuple, Integer>> s) throws Exception {
                String clusterLabel = s._1;
                SparkMLTuple point = s._2._1;
                SparkMLTuple center = clusterCenters.get(clusterLabel);
                double distance = DistanceMeasureService.getDistance(point, center, distanceMeasure);
                return new Tuple2<String, Tuple2<Double, Integer>>(clusterLabel, new Tuple2<Double, Integer>(distance, 1));
            }
        });

        JavaPairRDD<String, Tuple2<Double, Integer>> rdd5 = rdd4.reduceByKey(new Function2<Tuple2<Double, Integer>, Tuple2<Double, Integer>, Tuple2<Double, Integer>>() {
            public Tuple2<Double, Integer> call(Tuple2<Double, Integer> t1, Tuple2<Double, Integer> t2) throws Exception {
                return new Tuple2<Double, Integer>(t1._1 + t2._1, t1._2 + t2._2);
            }
        });

        JavaPairRDD<String, Double> inner_distance_rdd = rdd5.mapValues(new Function<Tuple2<Double, Integer>, Double>() {
            public Double call(Tuple2<Double, Integer> t) throws Exception {
                return t._1 / t._2;
            }
        });

        Map<String, Double> clusterDistance = inner_distance_rdd.collectAsMap();

        List<String> labels = new ArrayList<>();
        for(String label : clusterCenters.keySet()){
            labels.add(label);
        }

        double DB = Double.NEGATIVE_INFINITY;
        for(int i=0; i < labels.size(); ++i){
            String label_i = labels.get(i);
            double sigma_i = clusterDistance.get(label_i);
            SparkMLTuple center_i = clusterCenters.get(label_i);
            for(int j=i+1; j < labels.size(); ++j){
                String label_j = labels.get(j);

                double sigma_j = clusterDistance.get(label_j);
                SparkMLTuple center_j = clusterCenters.get(label_j);
                double inter_cluster_distance = DistanceMeasureService.getDistance(center_i, center_j, distanceMeasure);
                double C = (sigma_i + sigma_j) / inter_cluster_distance;
                DB = Math.max(C, DB);
            }
        }

        DB /= labels.size();

        return DB;

    }



    public int countClustersIn(JavaRDD<SparkMLTuple> batch){

        JavaPairRDD<String, Integer> rdd = batch.mapToPair(new PairFunction<SparkMLTuple, String, Integer>() {
            public Tuple2<String, Integer> call(SparkMLTuple tuple) throws Exception {
                String label = tuple.getPredictedLabelOutput();
                return new Tuple2<String, Integer>(label, 1);
            }
        });

        JavaPairRDD<String, Integer> rdd2 = rdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        return rdd2.collect().size();
    }

    private SparkMLTuple getCenter(JavaRDD<SparkMLTuple> points){


        SparkMLTuple center = points.reduce(new Function2<SparkMLTuple, SparkMLTuple, SparkMLTuple>() {
            public SparkMLTuple call(SparkMLTuple t1, SparkMLTuple t2) throws Exception {
                SparkMLTuple t = (SparkMLTuple)t2.clone();
                int dimension = t1.getDimension();
                for(int i=0; i < dimension; ++i){
                    if(t.containsKey(i) || t1.containsKey(i)){
                        t.put(i, t.getOrDefault(i, 0.0) + t1.getOrDefault(i, 0.0));
                    }
                }
                return t;
            }
        });

        return center;

    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        AbstractClustering rhs2 = (AbstractClustering)rhs;
        distanceMeasure = rhs2.distanceMeasure;
    }


    public BiFunction<SparkMLTuple, SparkMLTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<SparkMLTuple, SparkMLTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }





    public int getClusterOfClosestTuple(JavaSparkContext context, SparkMLTuple tuple, JavaRDD<SparkMLTuple> batch){

        final org.apache.spark.broadcast.Broadcast<SparkMLTuple> tupleBroadcast = context.broadcast(tuple);

        JavaPairRDD<SparkMLTuple, Double> rdd2 = batch.mapToPair(new PairFunction<SparkMLTuple, SparkMLTuple, Double>() {
            public Tuple2<SparkMLTuple, Double> call(SparkMLTuple point) throws Exception {
                final SparkMLTuple query = tupleBroadcast.getValue();
                double distance = DistanceMeasureService.getDistance(query, point, distanceMeasure);
                return new Tuple2<SparkMLTuple, Double>(query, distance);
            }
        });

        List<Tuple2<SparkMLTuple, Double>> output = rdd2.top(1, SparkMLTupleAscendComparator.INSTANCE);

        if(output.size() > 0){
            SparkMLTuple closestTuple = output.get(0)._1;
            Integer.parseInt(closestTuple.getPredictedLabelOutput());
        }
        return -1;
    }


}
