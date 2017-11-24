package com.github.chen0040.ml.sdk.modules;

import com.github.chen0040.ml.sdk.repositories.*;
import com.github.chen0040.ml.sdk.services.*;
import com.google.inject.AbstractModule;
import com.github.chen0040.ml.sdk.repositories.*;
import com.github.chen0040.ml.sdk.services.*;

/**
 * Created by xschen on 8/7/2016.
 */
public class SdkBasicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BasicBatchRepository.class).to(BasicBatchRepositoryImpl.class).asEagerSingleton();
        bind(MLModuleRepository.class).to(MLModuleRepositoryImpl.class).asEagerSingleton();
        bind(MLProjectRepository.class).to(MLProjectRepositoryImpl.class).asEagerSingleton();

        bind(BasicBatchService.class).to(BasicBatchServiceImpl.class);
        bind(MLModuleService.class).to(MLModuleServiceImpl.class);
        bind(MLProjectService.class).to(MLProjectServiceImpl.class);
    }
}
