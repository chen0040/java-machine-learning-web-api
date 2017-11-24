package com.github.chen0040.ml.services;

import com.github.chen0040.ml.models.AlgorithmModule;
import com.github.chen0040.ml.models.PredictedValue;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;


public interface MLSparkService {
	String getUrl();
	List<PredictedValue> tryGetAlgorithmModuleDataView(AlgorithmModule module, long currentTime);
	ListenableFuture<List<PredictedValue>> tryGetAlgorithmModuleDataViewAsync(final AlgorithmModule module, final long currentTime);
}
