package com.github.chen0040.ml.services;

import com.github.chen0040.ml.models.AlgorithmModule;
import com.github.chen0040.ml.models.PredictedValue;

import java.util.List;

/**
 * Created by root on 11/11/15.
 */
public interface MLDataService {
    List<PredictedValue> createData(AlgorithmModule module);
    void startScheduler();
}
