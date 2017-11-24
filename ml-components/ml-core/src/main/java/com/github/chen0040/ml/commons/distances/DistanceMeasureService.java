package com.github.chen0040.ml.commons.distances;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class DistanceMeasureService {
    public static double getDistance(IntelliContext mgr, IntelliTuple t1, IntelliTuple t2, BiFunction<IntelliTuple, IntelliTuple, Double> distance){
        if(distance== null){
            double[] x1 = mgr.toNumericArray(t1);
            double[] x2 = mgr.toNumericArray(t2);
            return euclideanDistance(x1, x2);
        }else{
            return distance.apply(t1, t2);
        }
    }

    public static double euclideanDistance(double[] x1, double[] x2){
        int dimension = Math.min(x1.length, x2.length);
        double cross_prod = 0;
        for(int i=0; i < dimension; ++i){
            cross_prod += (x1[i]-x2[i]) * (x1[i]-x2[i]);
        }
        return Math.sqrt(cross_prod);
    }

    public static Map<IntelliTuple, Double> getKNearestNeighbors(IntelliContext batch, IntelliTuple t, int k, BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure){
        Map<IntelliTuple, Double> neighbors = new HashMap<IntelliTuple, Double>();
        for(int i = 0; i < batch.tupleCount(); ++i){

            IntelliTuple ti = batch.tupleAtIndex(i);
            if(ti == t) continue;
            double distance = getDistance(batch, ti, t, distanceMeasure);
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

        return neighbors;
    }

    public static Object[] getKthNearestNeighbor(IntelliContext batch, IntelliTuple tuple, int k, BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        Map<IntelliTuple,Double> neighbors = getKNearestNeighbors(batch, tuple, k, distanceMeasure);

        double largest_distance = Double.MIN_VALUE;
        IntelliTuple neighbor_with_largest_distance = null;
        for(IntelliTuple tj : neighbors.keySet()){
            double tj_distance = neighbors.get(tj);
            if(tj_distance > largest_distance){
                largest_distance =tj_distance;
                neighbor_with_largest_distance = tj;
            }
        }

        return new Object[] {neighbor_with_largest_distance, largest_distance};
    }
}
