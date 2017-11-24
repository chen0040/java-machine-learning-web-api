package com.github.chen0040.ml.web.spring.controllers;

import com.alibaba.fastjson.JSON;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.services.MLProjectService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Created by root on 7/8/16.
 */
@RestController
@RequestMapping("projects")
public class ProjectController {

    private static Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private SdkServiceMediator mediator;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> saveProject(@RequestParam(value="project") String json){

        MLProject project = JSON.parseObject(json, MLProject.class);

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProject> future = projectService.saveAsync(project);
        Futures.addCallback(future, new FutureCallback<MLProject>() {
            public void onSuccess(MLProject mlProject) {
                ResponseEntity<MLProject> responseEntity =
                        new ResponseEntity<>(mlProject, HttpStatus.OK);
                deferredResult.setResult(responseEntity);
            }

            public void onFailure(Throwable throwable) {
                logger.error("Failed to fetch result from remote service", throwable);
                ResponseEntity<Void> responseEntity =
                        new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                deferredResult.setResult(responseEntity);
            }
        });

        return deferredResult;
    }
}
