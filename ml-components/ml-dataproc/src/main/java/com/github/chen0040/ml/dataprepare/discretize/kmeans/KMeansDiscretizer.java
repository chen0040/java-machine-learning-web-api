package com.github.chen0040.ml.dataprepare.discretize.kmeans;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.discrete.AbstractAttributeValueDiscretizer;
import com.github.chen0040.ml.dataprepare.discretize.DiscreteBatchUpdateResult;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class KMeansDiscretizer extends AbstractAttributeValueDiscretizer {

    private Map<Integer, KMeansFilter> filters;
    private int maxLevelCount = 10;


    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        KMeansDiscretizer rhs2 = (KMeansDiscretizer)rhs;
        maxLevelCount = rhs2.maxLevelCount;

        filters.clear();
        for(Integer index : rhs2.filters.keySet()){
            filters.put(index, (KMeansFilter)rhs2.filters.get(index).clone());
        }
    }

    @Override
    public void setInputDiscretizer(AttributeValueDiscretizer avd) {

    }

    @Override
    public AttributeValueDiscretizer getInputDiscretizer() {
        return null;
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

    @Override
    protected IntelliTuple discretize(IntelliTuple tuple, TupleAttributeLevelSource levels){

        IntelliContext context = getModelSource();

        if(!levels.hasAttributeNames()){
            for(int i = 0; i < tuple.tupleLength(); ++i){
                levels.setAttributeName(i, context.getAttributeName(i));
            }
        }


        Map<Integer, String> values = new HashMap<>();
        for(int i = 0; i < tuple.tupleLength(); ++i){
            if(context.isCategorical(i)){
                values.put(i, context.getAttributeValueAsLevel(tuple, i).getLevelName());
            }else {
                values.put(i, String.format("%d", discretize(context.getAttributeValueAsDouble(tuple, i, 0), i)));
            }

            levels.setAttributeMultiLevel(i, true);
        }

        IntelliTuple tuple2 = newTuple(values, null);

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

    private void initializeFilters(IntelliContext batch){
        IntelliTuple first = batch.tupleAtIndex(0);
        int n = first.tupleLength();
        for(int i=0; i < n; ++i){
            if(!batch.isCategorical(i)){
                filters.put(i, new KMeansFilter(i, maxLevelCount));
            }
        }
    }

    private KMeansFilter getFilter(int index){
        return filters.get(index);
    }

    public void add2filter(IntelliTuple tuple){
        IntelliContext context = getModelSource();
        for(Integer index : filters.keySet()){
            getFilter(index).addValue(context.getAttributeValueAsDouble(tuple, index, 0));
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        int m = batch.tupleCount();

        initializeFilters(batch);

        for(int i=0; i < m; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            add2filter(tuple);
        }

        for(KMeansFilter filter : filters.values()){
            filter.build();
        }

        return new DiscreteBatchUpdateResult();
    }


}
