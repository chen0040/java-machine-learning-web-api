package com.github.chen0040.ml.sdk.services;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.models.workflows.MLWorkflowNodeModelType;
import com.github.chen0040.ml.sdk.repositories.BasicBatchRepository;
import com.github.chen0040.ml.sdk.repositories.MLProjectRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.github.chen0040.ml.sdk.models.MLProjectOverview;
import com.github.chen0040.ml.sdk.models.workflows.MLWorkflow;
import com.github.chen0040.ml.sdk.models.workflows.MLWorkflowNode;
import com.github.chen0040.ml.sdk.repositories.MLModuleRepository;
import com.github.chen0040.ml.sdk.utils.RandomNameTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLProjectServiceImpl implements MLProjectService {

    @Inject
    MLProjectRepository repository;
    @Inject
    BasicBatchRepository batchRepository;
    @Inject
    MLModuleRepository moduleRepository;

    private static Logger logger = Logger.getLogger(String.valueOf(MLProjectServiceImpl.class));

    private ListeningExecutorService service;

    public MLProjectServiceImpl(){
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    public Collection<MLProject> findAll() {
        return repository.findAll();
    }

    public ListenableFuture<Collection<MLProject>> findAllAsync() {
        ListenableFuture<Collection<MLProject>> future = service.submit(new Callable<Collection<MLProject>>() {
            public Collection<MLProject> call() throws Exception {
                return findAll();
            }
        });
        return future;
    }

    public MLProject findById(String id) {
        return repository.findById(id);
    }

    public MLProject findByTitle(String title) {
        return repository.findByTitle(title);
    }

    public ListenableFuture<MLProject> findByIdAsync(final String id) {
        ListenableFuture<MLProject> future = service.submit(new Callable<MLProject>() {
            public MLProject call() throws Exception {
                return findById(id);
            }
        });
        return future;
    }

    public ListenableFuture<MLProject> findByTitleAsync(final String title) {
        ListenableFuture<MLProject> future = service.submit(new Callable<MLProject>() {
            public MLProject call() throws Exception {
                return findByTitle(title);
            }
        });
        return future;
    }


    public MLProject save(MLProject project){
        return repository.save(project);
    }

    public ListenableFuture<MLProject> saveAsync(final MLProject project) {
        ListenableFuture<MLProject> future = service.submit(new Callable<MLProject>() {
            public MLProject call() throws Exception {
                return save(project);
            }
        });
        return future;
    }

    public MLProject deleteById(String id) {
        return repository.deleteById(id);
    }

    public ListenableFuture<MLProject> deleteByIdAsync(final String id) {
        ListenableFuture<MLProject> future = service.submit(new Callable<MLProject>() {
            public MLProject call() throws Exception {
                return deleteById(id);
            }
        });
        return future;
    }

    public MLProjectOverview findOverviewById(String id){
        MLProject mlProject = findById(id);
        MLProjectOverview projectOverview = new MLProjectOverview();
        projectOverview.description = mlProject.getDescription();
        projectOverview.id = mlProject.getId();
        projectOverview.name = mlProject.getTitle();
        projectOverview.workflow = mlProject.getWorkflow().clone();

        return projectOverview;
    }

    public ListenableFuture<MLProjectOverview> findOverviewByIdAsync(final String id){
        ListenableFuture<MLProjectOverview> future = service.submit(new Callable<MLProjectOverview>() {
            public MLProjectOverview call() throws Exception {
                return findOverviewById(id);
            }
        });

        return future;
    }

    public Collection<MLProjectOverview> findAllOverviews(){
        Collection<MLProject> projects = findAll();
        Collection<MLProjectOverview> overviews = new ArrayList<MLProjectOverview>();

        for(MLProject proj : projects){
            MLProjectOverview overview = new MLProjectOverview();
            overview.id = proj.getId();
            overview.name = proj.getTitle();
            overview.description = proj.getDescription();
            overview.workflow = proj.getWorkflow().clone();
            overviews.add(overview);
        }

        return overviews;
    }

    public ListenableFuture<Collection<MLProjectOverview>> findAllOverviewsAsync(){
        ListenableFuture<Collection<MLProjectOverview>> future = service.submit(new Callable<Collection<MLProjectOverview>>() {
            public Collection<MLProjectOverview> call() throws Exception {
                return findAllOverviews();
            }
        });

        return future;
    }

    public MLWorkflow saveWorkflow(MLWorkflow workflow, String projectId) {
        MLProject proj = findById(projectId);
        proj.setWorkflow(workflow);
        proj = save(proj);
        return proj.getWorkflow();
    }

    public ListenableFuture<MLWorkflow> saveWorkflowAsync(final MLWorkflow workflow, final String projectId) {
        ListenableFuture<MLWorkflow> future = service.submit(new Callable<MLWorkflow>() {
            public MLWorkflow call() throws Exception {
                return saveWorkflow(workflow, projectId);
            }
        });
        return future;
    }

    public MLWorkflow runWorkflow(MLWorkflow workflow, String projectId) {
        try {
            boolean shouldSaveWorkflow = false;
            for (int i = 0; i < workflow.nodes.size(); ++i) {
                MLWorkflowNode node = workflow.nodes.get(i);
                if (node.modelType == MLWorkflowNodeModelType.Batch) {
                    if (node.modelId == null) {
                        IntelliContext batch = new IntelliContext();
                        batch.setProjectId(projectId);
                        batch.setTitle(RandomNameTool.randomName());
                        batch.setFormat(BasicBatchServiceImpl.INTERNAL);
                        batch.setTitle(node.name);
                        batch.setId(UUID.randomUUID().toString());
                        batch.setCreated(new Date());
                        batch.setDescription(batch.getTitle() + " is an internal data created in workflow");
                        batch = batchRepository.save(batch);
                        node.modelId = batch.getId();
                        shouldSaveWorkflow = true;
                    }
                }
            }

            if(shouldSaveWorkflow) {
                workflow = saveWorkflow(workflow, projectId);
            }

            workflow.processNode(new Function<String, MLModule>() {
                public MLModule apply(String moduleId) {
                    return moduleRepository.findById(moduleId);
                }
            }, new Function<String, IntelliContext>() {
                public IntelliContext apply(String batchId) {
                    return batchRepository.findById(batchId);
                }
            }, new Function<MLModule, MLModule>() {
                public MLModule apply(MLModule mlModule) {
                    return moduleRepository.save(mlModule);
                }
            }, new Function<IntelliContext, IntelliContext>() {
                public IntelliContext apply(IntelliContext intelliContext) {
                    return batchRepository.save(intelliContext);
                }
            });
        }catch(Exception ex){
            logger.log(Level.SEVERE, "runWorkflow failed", ex);
        }

        return workflow;
    }

    public ListenableFuture<MLWorkflow> runWorkflowAsync(final MLWorkflow workflow, final String projectId) {
        ListenableFuture<MLWorkflow> future = service.submit(new Callable<MLWorkflow>() {
            public MLWorkflow call() throws Exception {
                return runWorkflow(workflow, projectId);
            }
        });
        return future;
    }
}
