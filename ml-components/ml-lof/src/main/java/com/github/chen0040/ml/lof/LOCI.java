package com.github.chen0040.ml.lof;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 23/8/15.
 * Link:
 * http://repository.cmu.edu/cgi/viewcontent.cgi?article=1545&context=compsci
 * http://www.dbs.ifi.lmu.de/Publikationen/Papers/tutorial_slides.pdf
 */
public class LOCI extends AbstractAnomalyDetecter {
    private double r_max;

    private double alpha;
    private double k_sigma;

    private double[][] distanceMatrix;
    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LOCI rhs2 = (LOCI)rhs;
        r_max = rhs2.r_max;
        alpha = rhs2.alpha;
        k_sigma = rhs2.k_sigma;
        distanceMatrix = rhs2.distanceMatrix == null ? null : rhs2.distanceMatrix.clone();
        distanceMeasure = rhs2.distanceMeasure;
    }

    @Override
    public Object clone(){
        LOCI clone = new LOCI();
        clone.copy(this);

        return clone;
    }

    public LOCI(){
       alpha = 0.5;
        k_sigma = 3;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        if(tuple.getPredictedLabelOutput() != null){
            return AnomalyClassLabels.IS_ANOMALY.equals(tuple.getPredictedLabelOutput());
        }
        return false;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        int m = batch.tupleCount();

        distanceMatrix = new double[m][];
        for(int i=0; i < m; ++i) {
            distanceMatrix[i] = new double[m];
        }

        double maxDistance = Double.MIN_VALUE;
        for(int i=0; i < m; ++i){
            IntelliTuple tuple_i = batch.tupleAtIndex(i);
            for(int j=i+1; j < m; ++j){
                IntelliTuple tuple_j = batch.tupleAtIndex(j);
                double distance = DistanceMeasureService.getDistance(batch, tuple_i, tuple_j, distanceMeasure);
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance;
                maxDistance = Math.max(maxDistance, distance);
            }
        }

        r_max = maxDistance / alpha;

        List<List<Integer>> D = new ArrayList<List<Integer>>();

        for(int i=0; i < m; ++i){
            List<Integer> D_i = get_r_neighbors(i, r_max, distanceMatrix);
            D.add(D_i);
        }

        for(int i=0; i < m; ++i){
            List<Integer> D_i = D.get(i);
            int n = D_i.size();
            boolean isOutlier = false;
            for(int j=0; j < n; ++j){
                double r = distanceMatrix[i][D_i.get(j)];
                double alphar = alpha * r;
                int n_pi_alphar = get_alphar_neighbor_count(i, alphar, D_i, distanceMatrix);
                double nhat_pi_r_alpha = get_nhat_pi_r_alpha(i, alpha, r, D, distanceMatrix);
                double sigma_nhat_pi_r_alpha = get_sigma_nhat_pi_r_alpha(i, alpha, r, D, distanceMatrix, nhat_pi_r_alpha);
                double MDEF = 1 - n_pi_alphar / nhat_pi_r_alpha;
                double sigma_MDEF = sigma_nhat_pi_r_alpha / nhat_pi_r_alpha;

                if(MDEF  >  k_sigma *sigma_MDEF){
                    isOutlier = true;
                    break;
                }
            }

            IntelliTuple tuple = batch.tupleAtIndex(i);
            if(isOutlier){
                tuple.setPredictedLabelOutput(AnomalyClassLabels.IS_ANOMALY);
            }else{
                tuple.setPredictedLabelOutput(AnomalyClassLabels.IS_NOT_ANOMALY);
            }
        }



        return new BatchUpdateResult();
    }

    private double get_sigma_nhat_pi_r_alpha(int i, double alpha, double r, List<List<Integer>> D, double[][] distanceMatrix, double n_hat){
        List<Integer> D_i = D.get(i);
        int n_pi_r = D_i.size()+1; // including i itself
        double alphar = alpha * r;
        double sum = 0;
        for(Integer j : D_i){
            sum += Math.pow(get_alphar_neighbor_count(j, alphar, D.get(j), distanceMatrix) - n_hat, 2);
        }
        return Math.sqrt(sum / n_pi_r);
    }

    private double get_nhat_pi_r_alpha(int i, double alpha, double r, List<List<Integer>> D, double[][] distanceMatrix) {
        List<Integer> D_i = D.get(i);
        int n_pi_r = D_i.size()+1; // including i itself
        double alphar = alpha * r;
        double sum = 0;
        for(Integer j : D_i){
            sum += get_alphar_neighbor_count(j, alphar, D.get(j), distanceMatrix);
        }
        return sum / n_pi_r;
    }

    private int get_alphar_neighbor_count(int i, double alphar, List<Integer> d_i, double[][] distanceMatrix) {
        int count = 1; // including i itself
        for(Integer j : d_i){
            double distance = distanceMatrix[i][j];
            if(distance < alphar){
                count++;
            }
        }

        return count;
    }

    public List<Integer> get_r_neighbors(int i, double r, double[][] distanceMatrix){
        int m = distanceMatrix.length;
        List<Integer> rnn = new ArrayList<Integer>();
        for(int j=0; j < m; ++j){
            if(i==j) continue;
            double distance = distanceMatrix[i][j];
            if(distance < r){
                rnn.add(j);
            }
        }

        return rnn;
    }

    public void sort_r_neighbors(final int i, List<Integer> rnn, final double[][] distanceMatrix){
        // sort ascendingly based on critical distance
        Collections.sort(rnn, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                double critical_distance_1 = distanceMatrix[i][o1];
                double critical_distance_2 = distanceMatrix[i][o2];

                return Double.compare(critical_distance_1, critical_distance_2);
            }
        });
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
