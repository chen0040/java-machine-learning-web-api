package com.github.chen0040.ml.lof;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.commons.discrete.AbstractAttributeValueDiscretizer;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevel;
import com.github.chen0040.ml.dataprepare.discretize.kmeans.KMeansDiscretizer;
import com.github.chen0040.ml.commons.counts.CountRepository;

import java.util.*;

/**
 * Created by memeanalytics on 18/8/15.
 * Link:
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.20.4242&rep=rep1&type=pdf
 */
public class CBLOF extends AbstractAnomalyDetecter {

    private ArrayList<Cluster> clusters;

    private AttributeValueDiscretizer inputDiscretizer;
    private int split;

    public static final String THRESHOLD = "threshold";
    public static final String SHOULD_PARALLEL = "parallel";
    public static final String AUTO_THRESHOLDING = "automatic thresholding";
    public static final String AUTO_THRESHOLDING_RATIO = "anomaly ratio in automatic thresholding";
    public static final String SIMILARITY_THRESHOLD = "similary threshold";
    public static final String ALPHA = "alpha";
    public static final String BETA = "beta";



    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        CBLOF rhs2 = (CBLOF)rhs;
        split = rhs2.split;
        inputDiscretizer = rhs2.inputDiscretizer == null ? null : (AttributeValueDiscretizer)((AbstractAttributeValueDiscretizer)rhs2.inputDiscretizer).clone();

        clusters = null;
        if(rhs2.clusters != null){
            clusters = new ArrayList<Cluster>();
            for(int i=0; i < rhs2.clusters.size(); ++i){
                clusters.add((Cluster)rhs2.clusters.get(i).clone());
            }
        }
    }

    @Override
    public Object clone(){
        CBLOF clone = new CBLOF();
        clone.copy(this);

        return clone;
    }

    public CBLOF(){
        super();
        KMeansDiscretizer d = new KMeansDiscretizer();
        d.setMaxLevelCount(10);
        inputDiscretizer = d;
        _threshold(0.5);
        _alpha(0.8);
        _beta(0.1);
        _similarityThreshold(0.8);
        setAttribute(SHOULD_PARALLEL, 1);
        setAttribute(AUTO_THRESHOLDING, 1);
        setAttribute(AUTO_THRESHOLDING_RATIO, 0.05);
    }

    public double threshold(){
        return getAttribute(THRESHOLD);
    }

    private void _threshold(double threshold){
        this.setAttribute(THRESHOLD, threshold);
    }

    public double alpha() {
        return getAttribute(ALPHA);
    }

    public void _alpha(double alpha) {
        setAttribute(ALPHA, alpha);
    }

    public double beta() {
        return getAttribute(BETA);
    }

    public void _beta(double beta) {
        setAttribute(BETA, beta);
    }

    private double similarityThreshold() {
        return getAttribute(SIMILARITY_THRESHOLD);
    }

    private void _similarityThreshold(double similarityThreshold) {
        setAttribute(SIMILARITY_THRESHOLD, similarityThreshold);
    }

    public AttributeValueDiscretizer getInputDiscretizer() {
        return inputDiscretizer;
    }

    public void setInputDiscretizer(AttributeValueDiscretizer inputDiscretizer) {
        this.inputDiscretizer = inputDiscretizer;
    }

    @Override
    public boolean isAnomaly(IntelliTuple tuple) {
        double CBLOF = evaluate(tuple, getModelSource());
        return CBLOF > threshold();
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

        int selected_position = autoThresholdingCaps(orders.size());


        int last_index = orders.get(orders.size() - 1);
        int selected_index = -1;
        if(selected_position >= orders.size()){
            selected_position = orders.size() - 1;
            selected_index = last_index; //setAttribute(THRESHOLD, probs.get(orders.get(orders.size() - 1)));
        }
        else {
            selected_index = orders.get(selected_position);
        }

        while(probs.get(selected_index) == probs.get(last_index) && selected_position > 0){
            selected_position--;
            selected_index = orders.get(selected_position);
        }

        setAttribute(THRESHOLD, probs.get(selected_index));
    }

    protected boolean isAutoThresholding(){
        return (int)getAttribute(AUTO_THRESHOLDING) > 0;
    }

    protected int autoThresholdingCaps(int m){
        return Math.max(1, (int) (getAttribute(AUTO_THRESHOLDING_RATIO) * m));
    }

    private boolean parallel(){
        return (int)getAttribute(SHOULD_PARALLEL) > 0;
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) { this.setModelSource(batch);
        inputDiscretizer.batchUpdate(batch);

        runSqueezer(batch, similarityThreshold());

        // sort descendingly based on cluster size
        Collections.sort(clusters, new Comparator<Cluster>() {
            public int compare(Cluster o1, Cluster o2) {
                return Integer.compare(o2.size(), o1.size());
            }
        });

        for(int i=0; i < clusters.size(); ++i){
            Cluster cluster = clusters.get(i);
            cluster.setIndex(i);
            //System.out.println("cluster[" +i +"].size: " + cluster.size());
        }

        int m = batch.tupleCount();

        split = 0; // clusters with index < split will be the large clusters; otherwise small clusters
        int accumulated_count = 0;

        for(split = 0; split < clusters.size() - 1; ++split){
            int current_cluster_size = clusters.get(split).size();
            accumulated_count += current_cluster_size;
            if(accumulated_count >= m * alpha() && split != 0) break;
            int next_cluster_size = clusters.get(split+1).size();
            double ratio = (double)current_cluster_size / next_cluster_size;
            if(ratio  < beta() && split != 0) break;
        }

        //System.out.println("split: "+split);

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            TupleMetaData meta = (TupleMetaData)tuple.getTag();
            Cluster c = meta.cluster;

            if(c.getIndex() > split){ // c belongs to small clusters
                double minDistance = Double.MAX_VALUE;
                for(int j=0; j <= split; ++j){
                    double distance = clusters.get(j).distance(batch, tuple);
                    if(minDistance > distance){
                        minDistance = distance;
                    }
                }

                meta.CBLOF = c.size() * minDistance;
            }else{
                meta.CBLOF = c.size() * c.distance(batch, tuple);
            }

        }

        if(isAutoThresholding()){
            adjustThreshold(batch);
        }

        return new BatchUpdateResult();

    }

    private void runSqueezer(IntelliContext batch, double s){
        clusters = new ArrayList<Cluster>();

        int m = batch.tupleCount();
        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);

            if(i==0){
                clusters.add(new Cluster(batch, tuple));
            }
            else{
                double maxSim = Double.MIN_VALUE;
                Cluster closestCluster = null;
                for(Cluster c : clusters){
                    double sim = c.similarity(batch, tuple);
                    if(sim > maxSim){
                        maxSim = sim;
                        closestCluster = c;
                    }
                }

                if(maxSim < s){
                    clusters.add(new Cluster(batch, tuple));
                }else{
                    closestCluster.add(batch, tuple);
                }
            }


        }
    }

    public String getValue(IntelliTuple tuple, int j){
        String value;
        IntelliContext context = getModelSource();
        if(context.isCategorical(j)) {
            TupleAttributeLevel level = context.getAttributeValueAsLevel(tuple, j);
            value = level.getLevelName();
        }else{
            int discreteValue = inputDiscretizer.discretize(context.getAttributeValueAsDouble(tuple, j, 0), j);
            value = String.format("%d", discreteValue);
        }

        return value;
    }

    // the higher the CBLOF, the more likely the tuple is an outlier
    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        double CBLOF = Double.MAX_VALUE;

        double maxSim = Double.MIN_VALUE;
        Cluster closestCluster = null;
        for(Cluster c : clusters){
            double sim = c.similarity(context, tuple);
            if(sim > maxSim){
                maxSim = sim;
                closestCluster = c;
            }
        }

        if(closestCluster.getIndex() > split){ // c belongs to small clusters
            double minDistance = Double.MAX_VALUE;
            for(int j=0; j <= split; ++j){
                double distance = clusters.get(j).distance(context, tuple);
                if(minDistance > distance){
                    minDistance = distance;
                }
            }

            CBLOF = closestCluster.size() * minDistance;
        }else{
            CBLOF = closestCluster.size() * closestCluster.distance(context, tuple);
        }

        return CBLOF;
    }

    private class Cluster implements Cloneable {
        private CountRepository counts;
        private int totalCount;
        private int index;

        public void copy(Cluster rhs){
            counts = (CountRepository)rhs.counts.clone();
            totalCount = rhs.totalCount;
            index = rhs.index;
        }

        @Override
        public Object clone(){
            Cluster clone = new Cluster();
            clone.copy(this);

            return clone;
        }

        public Cluster(){
            counts = new CountRepository();
            totalCount = 0;
        }

        public Cluster(IntelliContext context, IntelliTuple tuple){
            counts = new CountRepository();
            totalCount = 0;
            add(context, tuple);
        }

        public int getIndex(){
            return this.index;
        }

        public void setIndex(int index){
            this.index = index;
        }

        public void add(IntelliContext context, IntelliTuple tuple){
            int n = tuple.tupleLength();
            for(int j=0; j < n; ++j){
                String value = getValue(tuple, j);
                String attributeName = context.getAttributeName(j);
                String eventName = attributeName+"="+value;

                counts.addSupportCount(eventName);
                counts.addSupportCount(attributeName);
            }
            tuple.setTag(new TupleMetaData(this, -1));
            totalCount++;
        }

        public int size(){
            return totalCount;
        }

        public double similarity(IntelliContext context, IntelliTuple tuple){
            int n = tuple.tupleLength();
            double similarity = 0;
            for(int j=0; j < n; ++j){
                String value = getValue(tuple, j);
                String attributeName = context.getAttributeName(j);
                String eventName = attributeName+"="+value;

                double count_Ai = counts.getSupportCount(eventName);
                double count_Total = counts.getSupportCount(attributeName);


                similarity += count_Ai / count_Total;
            }
            return similarity / n;
        }

        public double distance(IntelliContext context, IntelliTuple tuple){
            double sim = similarity(context, tuple);
            return 1 - sim;
        }
    }

    private class TupleMetaData{
        public Cluster cluster;
        public double CBLOF;

        public TupleMetaData(Cluster cluster, double lof){
            this.cluster = cluster;
            this.CBLOF = lof;
        }
    }
}
