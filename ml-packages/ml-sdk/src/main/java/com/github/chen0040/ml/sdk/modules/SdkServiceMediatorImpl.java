package com.github.chen0040.ml.sdk.modules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.github.chen0040.ml.sdk.services.BasicBatchService;
import com.github.chen0040.ml.sdk.services.MLModuleService;
import com.github.chen0040.ml.sdk.services.MLProjectService;

/**
 * Created by xschen on 8/7/2016.
 */
public class SdkServiceMediatorImpl implements SdkServiceMediator {

    private BasicBatchService batchService;
    private MLModuleService moduleService;
    private MLProjectService projectService;

    public SdkServiceMediatorImpl(){
        Injector injector = Guice.createInjector(new SdkBasicModule());
        batchService = injector.getInstance(BasicBatchService.class);
        moduleService = injector.getInstance(MLModuleService.class);
        projectService = injector.getInstance(MLProjectService.class);
    }

    @Override
    public BasicBatchService getBatchService() {
        return batchService;
    }

    @Override
    public MLModuleService getModuleService() {
        return moduleService;
    }

    @Override
    public MLProjectService getProjectService() {
        return projectService;
    }
}
