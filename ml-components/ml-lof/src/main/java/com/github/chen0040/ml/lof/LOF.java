package com.github.chen0040.ml.lof;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.distances.DistanceMeasureService;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 17/8/15.
 * Link:
 * http://www.dbs.ifi.lmu.de/Publikationen/Papers/LOF.pdf
 */
public class LOF extends AbstractAnomalyDetecter {

    public static final String THRESHOLD = "threshold";
    public static final String MIN_POINTS_LOWERBOUNDS = "minPtsLB";
    public static final String MIN_POINTS_UPPERBOUNDS = "maxPtsUB";
    public static final String SHOULD_PARALLEL = "parallel";
    public static final String AUTO_THRESHOLDING = "automatic thresholding";
    public static final String AUTO_THRESHOLDING_RATIO = "anomaly ratio in automatic thresholding";


    private static final Logger logger = Logger.getLogger(String.valueOf(LOF.class));

    public BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure;
    private double minScore;
    private double maxScore;
    private boolean addPredictedLabelAfterBatchUpdate = false;
    private IntelliContext model;
    private boolean parallel(){
        return (int)getAttribute(SHOULD_PARALLEL) > 0;
    }

    private int minPtsLB() {
         // min number for minPts;
        return (int)getAttribute(MIN_POINTS_LOWERBOUNDS);
    }
    private int minPtsUB(){
        // max number for minPts;
        return (int)getAttribute(MIN_POINTS_UPPERBOUNDS);
    }

    private void _minPtsLB(int value){
        setAttribute(MIN_POINTS_LOWERBOUNDS, value);
    }

    private void _minPtsUB(int value){
        setAttribute(MIN_POINTS_UPPERBOUNDS, value);
    }

    protected void adjustThreshold(IntelliContext batch){
        int m = batch.tupleCount();

        List<Integer> orders = new ArrayList<Integer>();
        List<Double> probs = new ArrayList<Double>();

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            double prob = evaluate(tuple, getModelSource());
            probs.add(prob);
            orders.add(i);
        }

        final List<Double> probs2 = probs;
        // sort descendingly by probability values
        Collections.sort(orders, new Comparator<Integer>() {
            public int compare(Integer h1, Integer h2) {
                double prob1 = probs2.get(h1);
                double prob2 = probs2.get(h2);
                return Double.compare(prob2, prob1);
            }
        });

        int selected_index = autoThresholdingCaps(orders.size());
        if(selected_index >= orders.size()){
            setAttribute(THRESHOLD, probs.get(orders.get(orders.size() - 1)));
        }
        else{
            setAttribute(THRESHOLD, probs.get(orders.get(selected_index)));
        }

    }

    public LOF(){
        super();
        setAttribute(THRESHOLD, 0.5);
        setSearchRange(3, 10);
        setAttribute(SHOULD_PARALLEL, 1);
        setAttribute(AUTO_THRESHOLDING, 1);
        setAttribute(AUTO_THRESHOLDING_RATIO, 0.05);
    }

    protected boolean isAutoThresholding(){
        return (int)getAttribute(AUTO_THRESHOLDING) > 0;
    }

    protected int autoThresholdingCaps(int m){
        return Math.max(1, (int) (getAttribute(AUTO_THRESHOLDING_RATIO) * m));
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public IntelliContext getModel(){
        return model;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        LOF rhs2 = (LOF)rhs;
        minScore = rhs2.minScore;
        maxScore = rhs2.maxScore;
        distanceMeasure = rhs2.distanceMeasure;
        addPredictedLabelAfterBatchUpdate = rhs2.addPredictedLabelAfterBatchUpdate;
        model = rhs2.model == null ? null : rhs2.model.clone();
    }

    @Override
    public Object clone(){
        LOF clone = new LOF();
        clone.copy(this);

        return clone;
    }

    private double threshold(){
        return getAttribute(THRESHOLD);
    }

    public MinPtsBounds searchRange() {
        return new MinPtsBounds(minPtsLB(), minPtsUB());
    }

    public void setSearchRange(int minPtsLB, int minPtsUB) {
        this._minPtsLB(minPtsLB);
        this._minPtsUB(minPtsUB);
    }

    public BiFunction<IntelliTuple, IntelliTuple, Double> getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(BiFunction<IntelliTuple, IntelliTuple, Double> distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        double score_lof = evaluate(tuple, getModelSource());
        return score_lof > threshold();
    }

    private class ScoreTask implements Callable<Double>{
        private IntelliContext batch;
        private IntelliTuple tuple;
        public ScoreTask(IntelliContext batch, IntelliTuple tuple){
            this.batch = batch;
            this.tuple = tuple;
        }

        public Double call() throws Exception {
            double score = score_lof_sync(batch, tuple);
            return score;
        }
    }
    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        this.model = batch.clone();

        int m = model.tupleCount();

        minScore = Double.MAX_VALUE;
        maxScore = Double.NEGATIVE_INFINITY;



        if(parallel()) {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<ScoreTask> tasks = new ArrayList<ScoreTask>();
            for (int i = 0; i < m; ++i) {
                tasks.add(new ScoreTask(model, model.tupleAtIndex(i)));
            }

            try {
                List<Future<Double>> results = executor.invokeAll(tasks);
                executor.shutdown();
                for (int i = 0; i < m; ++i) {
                    double score = results.get(i).get();
                    if(Double.isNaN(score)) continue;
                    if(Double.isInfinite(score)) continue;
                    minScore = Math.min(score, minScore);
                    maxScore = Math.max(score, maxScore);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else{
            for(int i=0; i < m; ++i){
                double score = score_lof_sync(model, model.tupleAtIndex(i));
                if(Double.isNaN(score)) continue;
                if(Double.isInfinite(score)) continue;
                minScore = Math.min(score, minScore);
                maxScore = Math.max(score, maxScore);
            }
        }

        if(isAutoThresholding()){
            adjustThreshold(model);
        }

        if(addPredictedLabelAfterBatchUpdate){
            for(int i=0; i < m; ++i){
                IntelliTuple tuple = model.tupleAtIndex(i);
                double score_lof = evaluate(tuple, batch);
                tuple.setPredictedLabelOutput(score_lof > threshold() ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY);
            }
        }


        return new BatchUpdateResult();
    }

    private class LOFTask implements Callable<Double>{
        private IntelliContext batch;
        private IntelliTuple tuple;
        private int minPts;

        public LOFTask(IntelliContext batch, IntelliTuple tuple, int minPts){
            this.batch = batch;
            this.tuple = tuple;
            this.minPts = minPts;
        }

        public Double call() throws Exception {
            double lof = local_outlier_factor(batch, tuple, minPts);
            return lof;
        }
    }

    private double score_lof_sync(IntelliContext batch, IntelliTuple tuple){
        double maxLOF = Double.NEGATIVE_INFINITY;

        for(int minPts = minPtsLB(); minPts <= minPtsUB(); ++minPts) { // the number of nearest neighbors used in defining the local neighborhood of the object.
            double lof = local_outlier_factor(batch, tuple, minPts);
            if(Double.isNaN(lof)) continue;
            maxLOF = Math.max(maxLOF, lof);
        }


        return maxLOF;
    }

    private double score_lof_async(IntelliContext batch, IntelliTuple tuple){
        if(!parallel()){
            return score_lof_sync(batch, tuple);
        }

        double maxLOF = 0;

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(8, minPtsUB() - minPtsLB() + 1));

        List<LOFTask> tasks = new ArrayList<LOFTask>();
        for(int minPts = minPtsLB(); minPts <= minPtsUB(); ++minPts) { // the number of nearest neighbors used in defining the local neighborhood of the object.
            tasks.add(new LOFTask(batch, tuple, minPts));
        }

        try {
            List <Future<Double>> results = executor.invokeAll(tasks);
            executor.shutdown();
            for(int i=0; i < results.size(); ++i){
                double lof = results.get(i).get();
                if(Double.isNaN(lof)) continue;
                if(Double.isInfinite(lof)) continue;
                maxLOF = Math.max(maxLOF, lof);
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "score_lof_async failed", e);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "score_lof_async failed", e);
        }


        return maxLOF;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context){
        double score = score_lof_async(model, tuple);

        //logger.info(String.format("score: %f minScore: %f, maxScore: %f", score, minScore, maxScore));

        score -= minScore;
        if(score < 0) score = 0;

        score /= (maxScore - minScore);

        if(score > 1) score = 1;

        return score;
    }

    private double evaluate_sync(IntelliTuple tuple, IntelliContext batch){
        double score = score_lof_sync(batch, tuple);

        score -= minScore;
        if(score < 0) score = 0;

        score /= (maxScore - minScore);

        if(score > 1) score = 1;

        return score;
    }

    public double k_distance(IntelliContext batch, IntelliTuple o, int k){
        Object[] result = DistanceMeasureService.getKthNearestNeighbor(batch, o, k, distanceMeasure);
        //IntelliTuple o_k = (IntelliTuple)result[0];
        double k_distance = (Double)result[1];
        return k_distance;
    }

    private double reach_dist(IntelliContext batch, IntelliTuple p, IntelliTuple o, int k){
        double distance_p_o = DistanceMeasureService.getDistance(batch, p, o, distanceMeasure);
        double distance_k_o = k_distance(batch, o, k);
        return Math.max(distance_k_o, distance_p_o);
    }

    private double local_reachability_density(IntelliContext batch, IntelliTuple p, int k){
        Map<IntelliTuple, Double> knn_p = DistanceMeasureService.getKNearestNeighbors(batch, p, k, distanceMeasure);
        double density = local_reachability_density(batch, p, k, knn_p);
        return density;
    }

    private double local_reachability_density(IntelliContext batch, IntelliTuple p, int k, Map<IntelliTuple, Double> knn_p){
        double sum_reach_dist = 0;
        for(IntelliTuple o : knn_p.keySet()){
            sum_reach_dist += reach_dist(batch, p, o, k);
        }
        double density = 1 / (sum_reach_dist / knn_p.size());
        return density;
    }

    // the higher this value, the more likely the point is an outlier
    public double local_outlier_factor(IntelliContext batch, IntelliTuple p, int k){

        Map<IntelliTuple, Double> knn_p = DistanceMeasureService.getKNearestNeighbors(batch, p, k, distanceMeasure);
        double lrd_p = local_reachability_density(batch, p, k, knn_p);
        double sum_lrd = 0;
        for(IntelliTuple o : knn_p.keySet()){
            sum_lrd += local_reachability_density(batch, o, k);
        }

        if(Double.isInfinite(sum_lrd) && Double.isInfinite(lrd_p)){
            return 1 / knn_p.size();
        }

        double lof = (sum_lrd / lrd_p) / knn_p.size();

        return lof;
    }

    public class MinPtsBounds{
        private int lowerBound;
        private int upperBound;

        public void setLowerBound(int lowerBound) {
            this.lowerBound = lowerBound;
        }

        public void setUpperBound(int upperBound) {
            this.upperBound = upperBound;
        }

        public MinPtsBounds(int lowerBounds, int upperBounds){
            lowerBound = lowerBounds;
            upperBound = upperBounds;
        }

        public int getLowerBound(){
            return lowerBound;
        }

        public int getUpperBound(){
            return upperBound;
        }
    }

}
