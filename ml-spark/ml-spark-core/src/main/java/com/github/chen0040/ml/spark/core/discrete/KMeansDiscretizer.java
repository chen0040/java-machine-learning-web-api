package com.github.chen0040.ml.spark.core.discrete;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class KMeansDiscretizer extends AbstractAttributeValueDiscretizer {

    private Map<Integer, KMeansFilter> filters;
    private int maxLevelCount = 10;
    private String modelSourceId;


    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        KMeansDiscretizer rhs2 = (KMeansDiscretizer)rhs;
        maxLevelCount = rhs2.maxLevelCount;
        modelSourceId = rhs2.modelSourceId;

        filters.clear();
        for(Integer index : rhs2.filters.keySet()){
            filters.put(index, (KMeansFilter)rhs2.filters.get(index).clone());
        }


    }

    @Override
    public Object clone(){
        KMeansDiscretizer clone = new KMeansDiscretizer();
        clone.copy(this);
        return clone;
    }

    public KMeansDiscretizer(){
        filters = new HashMap<Integer, KMeansFilter>();
    }

    public int getMaxLevelCount() {
        return maxLevelCount;
    }

    public void setMaxLevelCount(int maxLevelCount) {
        this.maxLevelCount = maxLevelCount;
    }

    public String getModelSourceId() {
        return modelSourceId;
    }

    public void setModelSourceId(String modelSourceId) {
        this.modelSourceId = modelSourceId;
    }

    @Override
    public SparkMLTuple discretize(SparkMLTuple tuple){

        SparkMLTuple tuple2 = new SparkMLTuple();
        for(int i=0; i < tuple.getDimension(); ++i){
            if(tuple.containsKey(i)) {
                if (columnsToDiscretize.contains(i)) {
                    tuple2.put(i, (double)discretize(tuple.get(i), i));
                } else {
                    tuple2.put(i, tuple.get(i));
                }
            }
        }

        tuple2.setLabelOutput(tuple.getLabelOutput());
        tuple2.setNumericOutput(tuple.getNumericOutput());
        tuple2.setPredictedLabelOutput(tuple.getPredictedLabelOutput());


        return tuple2;
    }

    @Override
    public int discretize(double value, int index) {
        if(filters.containsKey(index)){
            return filters.get(index).discretize(value);
        }else{
            return (int)value;
        }
    }

    private void initializeFilters(){
        for(Integer columnIndex : columnsToDiscretize){
            filters.put(columnIndex, new KMeansFilter(columnIndex, maxLevelCount));
        }
    }


    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {
        initializeFilters();

        for(Integer columnIndex : columnsToDiscretize){
            final int colIndex = columnIndex;
            JavaRDD<Double> column_rdd = batch.map(new Function<SparkMLTuple, Double>() {
                public Double call(SparkMLTuple tuple) throws Exception {
                    return tuple.getOrDefault(colIndex, 0.0);
                }
            });
            KMeansFilter filter = filters.get(columnIndex);
            filter.build(column_rdd);
        }

        return new BatchUpdateResult();
    }


}
