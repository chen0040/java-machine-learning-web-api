package com.github.chen0040.ml.lof;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;
import com.github.chen0040.ml.commons.IntelliTuple;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by memeanalytics on 19/8/15.
 * Link:
 * http://www.hutter1.net/ai/ldof.pdf
 */
public class LDOF extends AbstractAnomalyDetecter {

    private BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private int minPts; // k, namely the number of points in k-nearest neighborhood
    private int anomalyCount; // number of outliers to tupleAtIndex
    private int ldofLB; // lowerbounds for lodf, for pruning purpose
    private IntelliContext model;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LDOF rhs2 = (LDOF)rhs;
        minPts = rhs2.minPts;
        anomalyCount = rhs2.anomalyCount;
        ldofLB = rhs2.ldofLB;
        distanceMeasure = rhs2.distanceMeasure;
        model = rhs2.model == null ? null : rhs2.model.clone();
    }

    @Override
    public Object clone(){
        LDOF clone = new LDOF();
        clone.copy(this);

        return clone;
    }

    public LDOF(){
        super();
        minPts = 5;
        anomalyCount = 10;
    }

    public int getLdofLB() {
        return ldofLB;
    }

    public void setLdofLB(int ldofLB) {
        this.ldofLB = ldofLB;
    }

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public int getMinPts() {
        return minPts;
    }

    public void setMinPts(int minPts) {
        this.minPts = minPts;
    }

    public double knn_distance(IntelliTuple o, Map<IntelliTuple, Double> result){
        double avg_distance = 0;
        for(Double d : result.values()) {
            avg_distance += d;
        }
        avg_distance /= result.size();

        return avg_distance;
    }

    public List<IntelliTuple> toList(Collection<IntelliTuple> tc){
        List<IntelliTuple> list = new ArrayList<IntelliTuple>();
        for(IntelliTuple t : tc){
            list.add(t);
        }

        return list;
    }

    public double knn_inner_distance(IntelliContext context, IntelliTuple o, Map<IntelliTuple, Double> result){
        List<IntelliTuple> nn = toList(result.keySet());

        double distance_sum = 0;
        for(int i=0; i < nn.size(); ++i){
            for(int j=i+1; j < nn.size(); ++j){
                IntelliTuple ti = nn.get(i);
                IntelliTuple tj = nn.get(j);
                distance_sum += DistanceMeasureService.getDistance(context, ti, tj, distanceMeasure);
            }
        }
        distance_sum *= 2; //because of symmetry

        double avg_distance = distance_sum / ((nn.size()-1) * nn.size());

        return avg_distance;
    }

    public double local_distance_outlier_factor(IntelliContext batch, IntelliTuple p, int k){
        Map<IntelliTuple, Double> result = DistanceMeasureService.getKNearestNeighbors(batch, p, k, distanceMeasure);
        double knn_distance = knn_distance(p, result);
        double knn_inner_distance = knn_inner_distance(batch, p, result);

        return knn_distance / knn_inner_distance;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        if(tuple.getPredictedLabelOutput() != null) {
            return AnomalyClassLabels.IS_ANOMALY.equals(tuple.getPredictedLabelOutput());
        }

        return checkAnomaly(tuple);
    }

    public boolean checkAnomaly(IntelliTuple tuple){
        return local_distance_outlier_factor(model, tuple, minPts) > ldofLB; // recommended not to be invoked at this line
    }

    public IntelliContext getModel(){
        return model;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        this.model = batch.clone();

        List<IntelliTuple> maybeoutliers = new ArrayList<IntelliTuple>();
        final HashMap<IntelliTuple, Double> ldof_scores = new HashMap<IntelliTuple, Double>();

        int m = model.tupleCount();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = model.tupleAtIndex(i);

            double ldof = local_distance_outlier_factor(model, tuple, minPts);

            if(ldof >= ldofLB){
                maybeoutliers.add(tuple);
                ldof_scores.put(tuple, ldof);
            }
        }

        // sort descendingly based on the ldof score
        Collections.sort(maybeoutliers, new Comparator<IntelliTuple>() {
            public int compare(IntelliTuple o1, IntelliTuple o2) {
                double ldof1 = ldof_scores.get(o1);
                double ldof2 = ldof_scores.get(o2);
                return Double.compare(ldof2, ldof1);
            }
        });

        for(int i=0; i < anomalyCount && i < maybeoutliers.size(); ++i){
            maybeoutliers.get(i).setPredictedLabelOutput(AnomalyClassLabels.IS_ANOMALY);
        }


        return new BatchUpdateResult();
    }

    // the higher the ldof, the more likely the point is an outlier
    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {

        return local_distance_outlier_factor(model, tuple, minPts);
    }
}
