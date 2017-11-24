package com.github.chen0040.ml.sdk.repositories;

import com.github.chen0040.ml.commons.IntelliContext;

import java.util.Collection;

/**
 * Created by memeanalytics on 25/8/15.
 */
public interface BasicBatchRepository {
    IntelliContext findById(String id);
    IntelliContext deleteById(String id);
    Collection<IntelliContext> findAll();
    Collection<IntelliContext> findAllByProjectId(String projectId);
    IntelliContext save(IntelliContext batch);
}
