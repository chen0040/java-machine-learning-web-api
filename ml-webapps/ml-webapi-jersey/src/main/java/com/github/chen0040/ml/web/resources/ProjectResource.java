package com.github.chen0040.ml.web.resources;

import com.github.chen0040.ml.sdk.models.BasicBatchOverview;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.services.BasicBatchService;
import com.github.chen0040.ml.sdk.services.MLProjectService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.github.chen0040.ml.sdk.models.MLModuleOverview;
import com.github.chen0040.ml.sdk.models.MLProjectOverview;
import com.github.chen0040.ml.sdk.models.workflows.MLWorkflow;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.sdk.services.MLModuleService;
import org.glassfish.jersey.server.ManagedAsync;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 24/8/15.
 */
@Component
@Path("projects")
public class ProjectResource {


    @Context
    SdkServiceMediator mediator;

    private static Logger logger = Logger.getLogger(String.valueOf(ProjectResource.class));


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getProjects(@Suspended final AsyncResponse response){
        logger.log(Level.INFO, "begin: findAll()");
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<Collection<MLProject>> future = projectService.findAllAsync();
        Futures.addCallback(future, new FutureCallback<Collection<MLProject>>() {
            public void onSuccess(Collection<MLProject> projects) {
                logger.log(Level.INFO, "success: findAll(): "+projects.size());
                response.resume(projects);
            }

            public void onFailure(Throwable throwable) {
                logger.log(Level.INFO, "error: findAll(): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void saveProject(MLProject project, @Suspended final AsyncResponse response){
        //response.resume(projectService.saveFromHashMap(project));
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProject> future = projectService.saveAsync(project);
        Futures.addCallback(future, new FutureCallback<MLProject>() {
            public void onSuccess(MLProject mlProject) {
                response.resume(mlProject);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/overviews")
    @ManagedAsync
    public void getOverviews(@Suspended final AsyncResponse response){
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<Collection<MLProjectOverview>> future = projectService.findAllOverviewsAsync();
        Futures.addCallback(future, new FutureCallback<Collection<MLProjectOverview>>() {
            public void onSuccess(Collection<MLProjectOverview> mlProjectOverviews) {
                response.resume(mlProjectOverviews);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/workflows")
    @ManagedAsync
    public void saveWorkflow(@QueryParam("projectId") final String projectId, MLWorkflow workflow, @Suspended final AsyncResponse response){
        logger.info("begin saveWorkflow ...");

        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLWorkflow> future = projectService.saveWorkflowAsync(workflow, projectId);
        Futures.addCallback(future, new FutureCallback<MLWorkflow>() {
            public void onSuccess(MLWorkflow workflow2) {
                response.resume(workflow2);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/workflows/run")
    @ManagedAsync
    public void runWorkflow(@QueryParam("projectId") final String projectId, MLWorkflow workflow, @Suspended final AsyncResponse response){
        logger.info("begin runWorkflow ...");
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLWorkflow> future1 = projectService.runWorkflowAsync(workflow, projectId);
        Futures.addCallback(future1, new FutureCallback<MLWorkflow>() {
            public void onSuccess(MLWorkflow workflow2) {
                response.resume(workflow2);
            }

            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/overviews/{id}")
    @ManagedAsync
    public void getOverview(@PathParam("id") final String id, @Suspended final AsyncResponse response){
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProjectOverview> future1 = projectService.findOverviewByIdAsync(id);
        Futures.addCallback(future1, new FutureCallback<MLProjectOverview>() {
            public void onSuccess(MLProjectOverview mlProject) {
                final MLProjectOverview proj = mlProject;

                MLModuleService moduleService = mediator.getModuleService();
                ListenableFuture<Collection<MLModuleOverview>> future2 = moduleService.findAllOverviewsByProjectIdAsync(id);
                Futures.addCallback(future2, new FutureCallback<Collection<MLModuleOverview>>() {
                    public void onSuccess(Collection<MLModuleOverview> mlModules) {
                        proj.modules.addAll(mlModules);

                        BasicBatchService basicBatchService = mediator.getBatchService();
                        ListenableFuture<Collection<BasicBatchOverview>> future3 = basicBatchService.findAllOverviewsByProjectIdAsync(id);
                        Futures.addCallback(future3, new FutureCallback<Collection<BasicBatchOverview>>() {
                            public void onSuccess(Collection<BasicBatchOverview> batches) {
                                proj.batches.addAll(batches);
                                response.resume(proj);
                            }

                            public void onFailure(Throwable throwable) {
                                response.resume(throwable);
                            }
                        });
                    }

                    public void onFailure(Throwable throwable) {
                        response.resume(throwable);
                    }
                });
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
    public void deleteProject(@PathParam("id") String id, @Suspended final AsyncResponse response){
        //response.resume(projectService.deleteById(id));

        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProject> future = projectService.deleteByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<MLProject>() {
            public void onSuccess(MLProject project) {
                logger.log(Level.INFO, "success: deleteById(id): " + project);
                response.resume(project);
            }

            public void onFailure(Throwable throwable) {
                logger.log(Level.INFO, "error: deleteById(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getProject(@PathParam("id") String id, @Suspended final AsyncResponse response){
        //response.resume(projectService.findById(id));
        logger.log(Level.INFO, "begin findById(id)");
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProject> future = projectService.findByIdAsync(id);
        Futures.addCallback(future, new FutureCallback<MLProject>() {
            public void onSuccess(MLProject project) {
                logger.log(Level.INFO, "success: findById(id): " + project);
                response.resume(project);
            }

            public void onFailure(Throwable throwable) {
                logger.log(Level.INFO, "error: findById(id): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }

    @Path("/search/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getProjectByTitle(@PathParam("name") String name, @Suspended final AsyncResponse response){
        //response.resume(projectService.findById(id));
        logger.log(Level.INFO, "begin getProjectByTitle(name)");
        MLProjectService projectService = mediator.getProjectService();
        ListenableFuture<MLProject> future = projectService.findByTitleAsync(name);
        Futures.addCallback(future, new FutureCallback<MLProject>() {
            public void onSuccess(MLProject project) {
                logger.log(Level.INFO, "success: getProjectByTitle(name): " + project);
                response.resume(project);
            }

            public void onFailure(Throwable throwable) {
                logger.log(Level.INFO, "error: getProjectByTitle(name): " + throwable.getMessage());
                response.resume(throwable);
            }
        });
    }
}
