package com.github.chen0040.ml.spark.core;


import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class DistanceMeasureService implements Serializable {
    public static double getDistance(SparkMLTuple t1, SparkMLTuple t2, BiFunction<SparkMLTuple, SparkMLTuple, Double> distance){
        if(distance== null){
            return euclideanDistance(t1, t2);
        }else{
            return distance.apply(t1, t2);
        }
    }

    public static double euclideanDistance(SparkMLTuple x1, SparkMLTuple x2){
        int dimension = Math.min(x1.getDimension(), x2.getDimension());
        double cross_prod = 0;
        for(int i=0; i < dimension; ++i){
            double dx = (x1.getOrDefault(i, 0.0)-x2.getOrDefault(i, 0.0));
            cross_prod += dx * dx;
        }
        return Math.sqrt(cross_prod);
    }

    /*
    public static Map<Tuple, Double> getKNearestNeighbors(JavaRDD<SparkMLTuple> batch, SparkMLTuple t, int k, BiFunction<Tuple, Tuple, Double> distanceMeasure){
        Map<Tuple, Double> neighbors = new HashMap<Tuple, Double>();
        for(int i=0; i < batch.size(); ++i){

            Tuple ti = batch.tupleAtIndex(i);
            if(ti == t) continue;
            double distance = getDistance(ti, t, distanceMeasure);
            if(neighbors.size() < k){
                neighbors.put(ti, distance);
            }else{
                double largest_distance = Double.MIN_VALUE;
                Tuple neighbor_with_largest_distance = null;
                for(Tuple tj : neighbors.keySet()){
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

        return neighbors;
    }

    public static Object[] getKthNearestNeighbor(JavaRDD<SparkMLTuple> batch, SparkMLTuple tuple, int k, BiFunction<Tuple, Tuple, Double> distanceMeasure) {
        Map<Tuple,Double> neighbors = getKNearestNeighbors(batch, tuple, k, distanceMeasure);

        double largest_distance = Double.MIN_VALUE;
        Tuple neighbor_with_largest_distance = null;
        for(Tuple tj : neighbors.keySet()){
            double tj_distance = neighbors.get(tj);
            if(tj_distance > largest_distance){
                largest_distance =tj_distance;
                neighbor_with_largest_distance = tj;
            }
        }

        return new Object[] {neighbor_with_largest_distance, largest_distance};
    }*/
}
