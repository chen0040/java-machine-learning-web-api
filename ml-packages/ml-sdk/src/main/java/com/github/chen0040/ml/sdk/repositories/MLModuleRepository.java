package com.github.chen0040.ml.sdk.repositories;


import com.github.chen0040.ml.commons.MLModule;

import java.util.Collection;

/**
 * Created by memeanalytics on 24/8/15.
 */
public interface MLModuleRepository {
    MLModule save(MLModule module);
    MLModule deleteById(String id);
    MLModule findById(String id);
    Collection<MLModule> findAll();
    Collection<MLModule> findAllByProjectId(String projectId);
}
