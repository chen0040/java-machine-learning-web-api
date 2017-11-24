package com.github.chen0040.ml.commons.clustering;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;

/**
 * Created by memeanalytics on 17/8/15.
 */
public interface Clustering extends MLModule {
    int getCluster(IntelliTuple tuple);
    int getClusterOfClosestTuple(IntelliTuple tuple, IntelliContext batch);
    double computeDBI(IntelliContext batch);
    int getClusterCount(IntelliContext batch);
}
