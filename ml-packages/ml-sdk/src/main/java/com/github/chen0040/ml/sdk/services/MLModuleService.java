package com.github.chen0040.ml.sdk.services;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.sdk.models.MLModuleOverview;
import com.github.chen0040.ml.sdk.models.MLTestCase;
import com.github.chen0040.ml.sdk.models.MLTestCaseResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Map;

/**
 * Created by memeanalytics on 24/8/15.
 */
public interface MLModuleService {
    Collection<MLModule> findAll();
    ListenableFuture<Collection<MLModule>> findAllAsync();

    Collection<MLModule> findAllByProjectId(String projectId);
    ListenableFuture<Collection<MLModule>> findAllByProjectIdAsync(final String projectId);

    MLModule findById(String id);
    ListenableFuture<MLModule> findByIdAsync(final String id);

    MLModule save(MLModule module);
    ListenableFuture<MLModule> saveAsync(final MLModule module);
    MLModule saveFromMap(Map<String, Object> classifier);
    ListenableFuture<MLModule> saveFromMapAsync(final Map<String, Object> classifier);

    MLModule deleteById(String id);
    ListenableFuture<MLModule> deleteByIdAsync(final String id);

    MLModule create(MLProjectElementCreateCommand cmd);
    ListenableFuture<MLModule> createAsync(final MLProjectElementCreateCommand cmd);

    MLModule batchUpdate(String moduleId, String batchId);
    ListenableFuture<MLModule> batchUpdateAsync(final String moduleId, final String batchId);

    IntelliContext batchPredict(String moduleId, String batchId);
    ListenableFuture<IntelliContext> batchPredictAsync(final String moduleId, final String batchId);

    DataRow predict(Map<Integer, String> tuple, String moduleId);
    ListenableFuture<DataRow> predictAsync(final Map<Integer, String> tuple, final String moduleId);

    DataRow predict(DataRow row, String moduleId);
    ListenableFuture<DataRow> predictAsync(final DataRow row, final String moduleId);

    Collection<MLModuleOverview> findAllOverviewsByProjectId(String projectId);
    ListenableFuture<Collection<MLModuleOverview>> findAllOverviewsByProjectIdAsync(final String projectId);

    MLTestCaseResult batchTest(MLTestCase testCase);
    ListenableFuture<MLTestCaseResult> batchTestAsync(final MLTestCase testCase);



}
