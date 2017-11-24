package com.github.chen0040.ml.sdk.services;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.sdk.models.BasicBatchOverview;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.List;

/**
 * Created by memeanalytics on 25/8/15.
 */
public interface BasicBatchService {
    IntelliContext findById(String id);
    IntelliContext deleteById(String id);
    Collection<IntelliContext> findAll(boolean shellOnly);
    Collection<IntelliContext> findAllByProjectId(String projectId, boolean shellOnly);
    IntelliContext saveFromMap(Map<String, Object> batch);
    IntelliContext saveFromCsv(InputStream is, String projectId, String version, String splitBy, boolean hasHeader, int outputColumnLabel, int outputColumnNumeric, List<Integer> columns_to_add, List<Integer> columns_to_ignore, String title, String description);
    IntelliContext save(IntelliContext batch);

    ListenableFuture<IntelliContext> findByIdAsync(final String id);
    ListenableFuture<IntelliContext> deleteByIdAsync(final String id);
    ListenableFuture<Collection<IntelliContext>> findAllAsync(final boolean shellOnly);
    ListenableFuture<Collection<IntelliContext>> findAllByProjectIdAsync(final String projectId, final boolean shellOnly);
    ListenableFuture<IntelliContext> saveFromMapAsync(final Map<String, Object> batch);
    ListenableFuture<IntelliContext> saveAsync(final IntelliContext batch);
    ListenableFuture<IntelliContext> saveFromCsvAsync(final InputStream is, final String projectId, final String version, final String splitBy, final boolean hasHeader, final int outputColumnLabel, final int outputColumnNumeric, final List<Integer> columns_to_add, final List<Integer> columns_to_ignore, final String title, final String description);

    IntelliContext createByProjectId(String projectId);
    ListenableFuture<IntelliContext> createByProjectIdAsync(String projectId);

    Map<String, String> getSupportedFormats();
    ListenableFuture<Map<String, String>> getSupportedFormatsAsync();

    Collection<BasicBatchOverview> findAllOverviewsByProjectId(String projectId);
    ListenableFuture<Collection<BasicBatchOverview>> findAllOverviewsByProjectIdAsync(final String projectId);

    IntelliContext createBatchInProject(String projectId, Map<String, Object> parameters);
    ListenableFuture<IntelliContext> createBatchInProjectAsync(final String projectId, final Map<String, Object> parameters);

    IntelliTuple updateTuple(IntelliTuple tuple, String batchId);
    ListenableFuture<IntelliTuple> updateTupleAsync(final IntelliTuple tuple, final String batchId);

    IntelliContext saveTable(DataTable table);
    ListenableFuture<IntelliContext> saveTableAsync(DataTable table);
}
