package com.github.chen0040.ml.web.resources;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.sdk.services.BasicBatchService;
import com.github.chen0040.sk.utils.StringHelper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.*;

/**
 * Created by memeanalytics on 25/8/15.
 */
@Component
@Path("batches")
public class DataResource {

    private static final Logger logger = LoggerFactory.getLogger(String.valueOf(DataResource.class));

    @Context
    SdkServiceMediator mediator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void findAll(@QueryParam("light") final String light, @Suspended final AsyncResponse response){

        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<Collection<IntelliContext>> future = service.findAllAsync(light != null && light.equals("1"));
        Futures.addCallback(future, new FutureCallback<Collection<IntelliContext>>() {
            public void onSuccess(Collection<IntelliContext> intelliContexts) {
                response.resume(intelliContexts);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @Path("/formats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void getSupportedFormats(@Suspended final AsyncResponse response){
        BasicBatchService service = mediator.getBatchService();

        ListenableFuture<Map<String, String>> future = service.getSupportedFormatsAsync();
        Futures.addCallback(future, new FutureCallback<Map<String, String>>() {
            public void onSuccess(Map<String, String> hmap) {
                response.resume(hmap);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }


    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void findAllByProjectId(@QueryParam("projectId") String projectId, @QueryParam("light") final String light, @Suspended final AsyncResponse response){
        BasicBatchService service = mediator.getBatchService();

        ListenableFuture<Collection<IntelliContext>> future = service.findAllByProjectIdAsync(projectId, light != null && light.equals("1"));
        Futures.addCallback(future, new FutureCallback<Collection<IntelliContext>>() {
            public void onSuccess(Collection<IntelliContext> intelliContexts) {
                response.resume(intelliContexts);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @Path("/project")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void createBatchInProject(@QueryParam("projectId") String projectId, Map<String, Object> parameters, @Suspended final AsyncResponse response){
        BasicBatchService service = mediator.getBatchService();

        ListenableFuture<IntelliContext> future = service.createBatchInProjectAsync(projectId, parameters);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext intelliContext) {
                response.resume(intelliContext);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void save(final Map<String, Object> batch, @Suspended final AsyncResponse response) {

        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.saveFromMapAsync(batch);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext tuples)
            {
                response.resume(tuples);
            }

            public void onFailure(Throwable throwable) {
                logger.error("error: saveFromMap(batch) ", throwable);
                response.resume(throwable);
            }
        });
    }

    @Path("/tuples")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @ManagedAsync
    public void updateTuple(@QueryParam("batchId") final String batchId, final IntelliTuple tuple, @Suspended final AsyncResponse response){
        logger.info("begin updateTuple()");
        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliTuple> future = service.updateTupleAsync(tuple, batchId);
        Futures.addCallback(future, new FutureCallback<IntelliTuple>() {
            public void onSuccess(IntelliTuple intelliTuple) {
                response.resume(intelliTuple);
            }

            public void onFailure(Throwable throwable) {
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
        logger.info("begin create(id)");
        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.createByProjectIdAsync(cmd.getProjectId());
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext batch) {
                logger.info("success: create(id): " + batch);
                response.resume(batch);
            }

            public void onFailure(Throwable throwable) {
                logger.info("error: create(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @Path("/table")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createByTable(DataTable table, @Suspended final AsyncResponse response){
        //response.resume(moduleService.findById(id));
        logger.info("begin createByTable()");
        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.saveTableAsync(table);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext batch) {
                logger.info("success: createByTable(id): " + batch);
                response.resume(batch);
            }

            public void onFailure(Throwable throwable) {
                logger.info("error: createByTable(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void findById(@PathParam("id") String id, @Suspended final AsyncResponse response){
        BasicBatchService service = mediator.getBatchService();
        ListenableFuture<IntelliContext> future = service.findByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext mlModule) {
                response.resume(mlModule);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    private List<Integer> parse4list(String data){
        List<Integer> values = new ArrayList<Integer>();
        if(data==null || data.equals("")) return values;

        String[] comps = data.split(",");
        for(String comp : comps){
            int value = -1;
            try{
                value = Integer.parseInt(comp.trim());
            }catch (NumberFormatException ex){
                logger.warn(ex.getMessage());
            }
            if(value > -1){
                values.add(value);
            }
        }
        return values;
    }

    private boolean parseBoolean(String flag){
        if(flag==null) return false;
        return flag.equals("1");
    }

    @POST
    @Path("/csv")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ManagedAsync
    public void saveCsv(@FormDataParam("input_data") InputStream fileInputStream,
                        @FormDataParam("splitBy") String splitBy,
                        @FormDataParam("hasHeader") String hasHeader,
                        @FormDataParam("projectId") String projectId,
                        @FormDataParam("output_column_label") String outputColumnLabel,
                        @FormDataParam("output_column_numeric") String outputColumnNumeric,
                        @FormDataParam("columns_to_add") String columnsToAdd,
                        @FormDataParam("columns_to_ignore") String columnsToIgnore,
                        @FormDataParam("version") String version,
                        @FormDataParam("title") String title,
                        @FormDataParam("description") String description,
                        @Suspended final AsyncResponse response){

        int output_column_label = StringHelper.parseInteger(outputColumnLabel, -1);
        int output_column_numeric = StringHelper.parseInteger(outputColumnNumeric, -1);

        List<Integer> columns_to_add = parse4list(columnsToAdd);
        List<Integer> columns_to_ignore = parse4list(columnsToIgnore);

        boolean has_header = parseBoolean(hasHeader);

        if(splitBy == null) splitBy = ",";

        BasicBatchService service = mediator.getBatchService();

        ListenableFuture<IntelliContext> future = service.saveFromCsvAsync(fileInputStream, projectId, version,
                splitBy, has_header,
                output_column_label, output_column_numeric,
                columns_to_add, columns_to_ignore,
                title, description);

        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext tuples) {
                logger.info("Success in saving data: {}", JSON.toJSONString(tuples, SerializerFeature.BrowserCompatible));
                response.resume(tuples);
            }

            public void onFailure(Throwable throwable) {
                logger.error("error: saveFromMap(batch): " + throwable.getMessage());
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

        BasicBatchService service = mediator.getBatchService();

        ListenableFuture<IntelliContext> future = service.deleteByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<IntelliContext>() {
            public void onSuccess(IntelliContext classifier) {
                logger.info("success: deleteById(id): " + classifier);
                response.resume(classifier);
            }

            public void onFailure(Throwable throwable) {
                logger.info("error: deleteById(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }
}
