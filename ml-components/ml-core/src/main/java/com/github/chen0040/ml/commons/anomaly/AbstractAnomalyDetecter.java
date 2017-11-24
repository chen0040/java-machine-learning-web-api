package com.github.chen0040.ml.commons.anomaly;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.AbstractMLModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 13/8/15.
 */
public abstract class AbstractAnomalyDetecter extends AbstractMLModule implements AnomalyDetector {

    public AbstractAnomalyDetecter(){
        super();
        setIsLabelRequiredInBatchUpdate(false);
    }

    public List<IntelliTuple> findOutliers(IntelliContext batch){
        int n = batch.tupleCount();
        List<IntelliTuple> outliers = new ArrayList<IntelliTuple>();
        for(int i=0; i < n; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            if(isAnomaly(tuple))
            {
                outliers.add(tuple);
            }
        }
        return outliers;
    }


    public List<Integer> findOutlierPositions(IntelliContext batch){
        int n = batch.tupleCount();
        List<Integer> outliers = new ArrayList<Integer>();
        for(int i=0; i < n; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            if(isAnomaly(tuple))
            {
                outliers.add(i);
            }
        }
        return outliers;
    }

    public abstract boolean isAnomaly(IntelliTuple tuple);
}
