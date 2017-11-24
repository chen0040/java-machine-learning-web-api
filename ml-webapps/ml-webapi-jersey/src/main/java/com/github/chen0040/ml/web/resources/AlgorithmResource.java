package com.github.chen0040.ml.web.resources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.sdk.models.MLTestCase;
import com.github.chen0040.ml.sdk.models.MLTestCaseResult;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.sdk.services.MLModuleFactory;
import com.github.chen0040.ml.sdk.services.MLModuleService;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.Map;

/**
 * Created by memeanalytics on 24/8/15.
 */
@Component
@Path("mlmodules")
public class AlgorithmResource {


    @Context
    SdkServiceMediator mediator;

    private static Logger logger = LoggerFactory.getLogger(AlgorithmResource.class);

    @Path("/project")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void findByProject(@QueryParam("projectId") String projectId, @Suspended final AsyncResponse response){
        logger.info( "begin: findAllByProjectId(projectId): "+projectId);

        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<Collection<MLModule>> future = moduleService.findAllByProjectIdAsync(projectId);
        Futures.addCallback(future, new FutureCallback<Collection<MLModule>>() {
            public void onSuccess(Collection<MLModule> classifiers) {
                logger.info( "success: findAllByProjectId(projectId): "+classifiers.size());
                response.resume(classifiers);
            }

            public void onFailure(Throwable throwable) {
                logger.info( "error: findAllByProjectId(projectId): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void findAll(@Suspended final AsyncResponse response){
        logger.info( "begin: findAll()");
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<Collection<MLModule>> future = moduleService.findAllAsync();
        Futures.addCallback(future, new FutureCallback<Collection<MLModule>>() {
            public void onSuccess(Collection<MLModule> classifiers) {
                logger.info( "success: findAll(): "+classifiers.size());
                response.resume(classifiers);
            }

            public void onFailure(Throwable throwable) {
                logger.info( "error: findAll(): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void save(Map<String, Object> classifier, @Suspended final AsyncResponse response){

        //response.resume(moduleService.saveFromMap(classifier));
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLModule> future = moduleService.saveFromMapAsync(classifier);
        Futures.addCallback(future, new FutureCallback<MLModule>() {
            public void onSuccess(MLModule MLModule) {
                response.resume(MLModule);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @GET
      @Path("/{id}")
      @Produces(MediaType.APPLICATION_JSON)
      @ManagedAsync
      public void findById(@PathParam("id") String id, @Suspended final AsyncResponse response){
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLModule> future = moduleService.findByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<MLModule>() {
            public void onSuccess(MLModule mlModule) {
                response.resume(mlModule);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @GET
    @Path("/batch-update")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void batchUpdate(@QueryParam("moduleId") String moduleId, @QueryParam("batchId") String batchId, @Suspended final AsyncResponse response){
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLModule> future = moduleService.batchUpdateAsync(moduleId, batchId);
        Futures.addCallback(future, new FutureCallback<MLModule>() {
            public void onSuccess(MLModule mlModule) {
                if(mlModule != null) {
                    logger.info("Success in batch update!");
                    logger.info(JSON.toJSONString(mlModule, SerializerFeature.BrowserCompatible));
                } else {
                    logger.info("batchUpdate returns null!");
                }
                response.resume(mlModule);
            }

            public void onFailure(Throwable throwable) {
                logger.error("Failure in batch Update");
                response.resume(throwable);
            }
        });
    }

    @GET
    @Path("/batch-predict")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void batchPredict(@QueryParam("moduleId") String moduleId, @QueryParam("batchId") String batchId, @Suspended final AsyncResponse response){
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<IntelliContext> future = moduleService.batchPredictAsync(moduleId, batchId);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext batch) {
                response.resume(batch);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Path("/batch-test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void batchTest(final MLTestCase testCase, @Suspended final AsyncResponse response){
        //logger.info("batchTest begin ...");
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLTestCaseResult> future = moduleService.batchTestAsync(testCase);
        Futures.addCallback(future, new FutureCallback<MLTestCaseResult>() {
            public void onSuccess(MLTestCaseResult mlTestCaseResult) {
                response.resume(mlTestCaseResult);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @GET
    @Path("/prototypes")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void getPrototypes(@Suspended final AsyncResponse response){
        ListenableFuture<Map<String, List<String>>> future = MLModuleFactory.getInstance().getPrototypeMapAsync();
        Futures.addCallback(future, new FutureCallback<Map<String, List<String>>>() {
            public void onSuccess(Map<String, List<String>> hmap) {
                response.resume(hmap);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Path("/match")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void predictRow(@QueryParam("moduleId") String moduleId, Map<Integer, String> row, @Suspended final AsyncResponse response){
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<DataRow> future = moduleService.predictAsync(row, moduleId);
        Futures.addCallback(future, new FutureCallback<DataRow>() {
            public void onSuccess(DataRow s) {
                response.resume(s);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Path("/predict-row")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void predictDataRow(@QueryParam("moduleId") String moduleId, DataRow row, @Suspended final AsyncResponse response){
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<DataRow> future = moduleService.predictAsync(row, moduleId);
        Futures.addCallback(future, new FutureCallback<DataRow>() {
            public void onSuccess(DataRow s) {
                response.resume(s);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @ManagedAsync
    public void deleteById(@PathParam("id") String id, @Suspended final AsyncResponse response){
        //response.resume(moduleService.deleteById(id));

        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLModule> future = moduleService.deleteByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<MLModule>() {
            public void onSuccess(MLModule classifier) {
                logger.info( "success: deleteById(id): " + classifier);
                response.resume(classifier);
            }

            public void onFailure(Throwable throwable) {
                logger.info( "error: deleteById(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @Path("/factory")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void create(MLProjectElementCreateCommand cmd, @Suspended final AsyncResponse response){
        //response.resume(moduleService.findById(id));
        logger.info( "begin create(id)");
        MLModuleService moduleService = mediator.getModuleService();
        ListenableFuture<MLModule> future = moduleService.createAsync(cmd);
        Futures.addCallback(future, new FutureCallback<MLModule>() {
            public void onSuccess(MLModule classifier) {
                logger.info( "success: create(id): " + classifier);
                response.resume(classifier);
            }

            public void onFailure(Throwable throwable) {
                logger.info( "error: create(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }
}
