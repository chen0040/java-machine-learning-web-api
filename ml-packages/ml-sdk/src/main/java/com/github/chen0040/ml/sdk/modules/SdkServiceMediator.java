package com.github.chen0040.ml.sdk.modules;

import com.github.chen0040.ml.sdk.services.BasicBatchService;
import com.github.chen0040.ml.sdk.services.MLModuleService;
import com.github.chen0040.ml.sdk.services.MLProjectService;

/**
 * Created by xschen on 8/7/2016.
 */
public interface SdkServiceMediator {
    BasicBatchService getBatchService();
    MLModuleService getModuleService();
    MLProjectService getProjectService();
}
