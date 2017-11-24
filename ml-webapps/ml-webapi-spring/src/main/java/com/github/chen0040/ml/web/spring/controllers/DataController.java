package com.github.chen0040.ml.web.spring.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.sdk.services.BasicBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.Map;

/**
 * Created by root on 7/8/16.
 */
@RestController
@RequestMapping("batches")
public class DataController {

    private static final Logger logger = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private SdkServiceMediator mediator;

    @RequestMapping(value="", method= RequestMethod.GET)
    public DeferredResult<ResponseEntity<?>> findAll(@RequestParam(value="light") final String light){

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<Collection<IntelliContext>> future = service.findAllAsync(light != null && light.equals("1"));
        Futures.addCallback(future, new FutureCallback<Collection<IntelliContext>>() {
            public void onSuccess(Collection<IntelliContext> intelliContexts) {

                ResponseEntity<Collection<IntelliContext>> responseEntity =
                        new ResponseEntity<>(intelliContexts, HttpStatus.OK);
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

    @RequestMapping(value="factory", method= RequestMethod.POST)
    public  DeferredResult<ResponseEntity<?>> create(@RequestParam(value="cmd") String json){

        MLProjectElementCreateCommand cmd = JSON.parseObject(json, MLProjectElementCreateCommand.class);

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        logger.info("begin create(id)");
        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.createByProjectIdAsync(cmd.getProjectId());
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext batch) {
                ResponseEntity<IntelliContext> responseEntity =
                        new ResponseEntity<>(batch, HttpStatus.OK);
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

    @RequestMapping(value="", method= RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> save(@RequestParam(value="batch") String json) {

        Map<String, Object> batch = JSON.parseObject(json, new TypeReference<Map<String, Object>>(){}.getType());

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.saveFromMapAsync(batch);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext tuples)
            {
                ResponseEntity<IntelliContext> responseEntity =
                        new ResponseEntity<>(tuples, HttpStatus.OK);
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
