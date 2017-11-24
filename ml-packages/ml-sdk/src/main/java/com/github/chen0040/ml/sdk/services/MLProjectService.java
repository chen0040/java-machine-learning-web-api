package com.github.chen0040.ml.sdk.services;

import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.models.MLProjectOverview;
import com.github.chen0040.ml.sdk.models.workflows.MLWorkflow;

import java.util.Collection;

/**
 * Created by memeanalytics on 24/8/15.
 */
public interface MLProjectService {
    Collection<MLProject> findAll();
    ListenableFuture<Collection<MLProject>> findAllAsync();

    MLProject findById(String id);
    ListenableFuture<MLProject> findByIdAsync(final String id);

    MLProject save(MLProject project);
    ListenableFuture<MLProject> saveAsync(final MLProject project);

    MLProject deleteById(String id);
    ListenableFuture<MLProject> deleteByIdAsync(final String id);

    MLProjectOverview findOverviewById(String id);
    ListenableFuture<MLProjectOverview> findOverviewByIdAsync(String id);

    Collection<MLProjectOverview> findAllOverviews();
    ListenableFuture<Collection<MLProjectOverview>> findAllOverviewsAsync();

    MLWorkflow saveWorkflow(MLWorkflow workflow, String projectId);
    ListenableFuture<MLWorkflow> saveWorkflowAsync(final MLWorkflow workflow, final String projectId);

    MLWorkflow runWorkflow(MLWorkflow workflow, String projectId);
    ListenableFuture<MLWorkflow> runWorkflowAsync(final MLWorkflow workflow, String projectId);

    MLProject findByTitle(String name);
    ListenableFuture<MLProject> findByTitleAsync(final String name);
}
