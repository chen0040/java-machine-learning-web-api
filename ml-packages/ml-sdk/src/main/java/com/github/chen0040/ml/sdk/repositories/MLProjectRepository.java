package com.github.chen0040.ml.sdk.repositories;

import com.github.chen0040.ml.sdk.models.MLProject;

import java.util.Collection;

/**
 * Created by memeanalytics on 24/8/15.
 */
public interface MLProjectRepository {
    Collection<MLProject> findAll();
    MLProject findById(String id);
    MLProject save(MLProject project);
    MLProject deleteById(String id);

    MLProject findByTitle(String title);
}
