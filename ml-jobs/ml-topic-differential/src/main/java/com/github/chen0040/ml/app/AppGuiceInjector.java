package com.github.chen0040.ml.app;

import com.github.chen0040.ml.repositories.AlgorithmModuleRepository;
import com.github.chen0040.ml.repositories.AlgorithmModuleRepositoryCassandra;
import com.github.chen0040.ml.repositories.TopicModelRepository;
import com.github.chen0040.ml.services.*;
import com.google.inject.AbstractModule;
import com.github.chen0040.ml.repositories.TopicModelRepositoryCassandra;
import com.github.chen0040.ml.services.*;

/**
 * Created by root on 11/11/15.
 */
public class AppGuiceInjector extends AbstractModule {

    @Override
    protected void configure() {
        bind(AlgorithmModuleRepository.class).to(AlgorithmModuleRepositoryCassandra.class);
        bind(TopicModelRepository.class).to(TopicModelRepositoryCassandra.class);

        bind(AlgorithmModuleService.class).to(AlgorithmModuleServiceImpl.class);
        bind(MLSparkService.class).to(MLSparkServiceImpl.class);
        bind(TopicModelService.class).to(TopicModelServiceImpl.class);

        bind(MLDataService.class).to(MLDataServiceImpl.class);
    }
}
