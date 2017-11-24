package com.github.chen0040.ml.sdk.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.MLModuleOutputType;
import com.github.chen0040.ml.commons.anomaly.AnomalyDetector;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.commons.classifiers.Classifier;
import com.github.chen0040.ml.commons.clustering.Clustering;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.commons.regressions.Regression;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.sdk.models.MLTestCase;
import com.github.chen0040.ml.sdk.models.MLTestCaseResult;
import com.github.chen0040.ml.sdk.models.TestingMode;
import com.github.chen0040.ml.sdk.repositories.BasicBatchRepository;
import com.github.chen0040.ml.sdk.models.MLModuleOverview;
import com.github.chen0040.ml.sdk.repositories.MLModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLModuleServiceImpl implements MLModuleService{

    private static final Logger logger = LoggerFactory.getLogger(String.valueOf(MLModuleServiceImpl.class));

    @Inject
    MLModuleRepository repository;

    @Inject
    BasicBatchRepository basicBatchRepository;

    private ListeningExecutorService service;

    public MLModuleServiceImpl(){
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    public Collection<MLModule> findAll() {
        return repository.findAll();
    }

    public ListenableFuture<Collection<MLModule>> findAllAsync() {
        ListenableFuture<Collection<MLModule>> future = service.submit(new Callable<Collection<MLModule>>() {
            public Collection<MLModule> call() throws Exception {
                return findAll();
            }
        });
        return future;
    }

    public Collection<MLModule> findAllByProjectId(String projectId) {
        return repository.findAllByProjectId(projectId);
    }

    public ListenableFuture<Collection<MLModule>> findAllByProjectIdAsync(final String projectId) {
        ListenableFuture<Collection<MLModule>> future = service.submit(new Callable<Collection<MLModule>>() {
            public Collection<MLModule> call() throws Exception {
                return findAllByProjectId(projectId);
            }
        });
        return future;
    }

    public MLModule findById(String id) {
        return repository.findById(id);
    }

    public ListenableFuture<MLModule> findByIdAsync(final String id) {
        ListenableFuture<MLModule> future = service.submit(new Callable<MLModule>() {
            public MLModule call() throws Exception {
                return findById(id);
            }
        });
        return future;
    }


    public MLModule saveFromMap(Map<String, Object> data){
        MLModule module = MLModuleFactory.getInstance().convert(data);
        return save(module);
    }

    public ListenableFuture<MLModule> saveFromMapAsync(final Map<String, Object> module) {
        ListenableFuture<MLModule> future = service.submit(new Callable<MLModule>() {
            public MLModule call() throws Exception {
                return saveFromMap(module);
            }
        });
        return future;
    }

    public ListenableFuture<MLModule> saveAsync(final MLModule module) {
        ListenableFuture<MLModule> future = service.submit(new Callable<MLModule>() {
            public MLModule call() throws Exception {
                return save(module);
            }
        });
        return future;
    }

    public MLModule deleteById(String id) {
        return repository.deleteById(id);
    }

    public ListenableFuture<MLModule> deleteByIdAsync(final String id) {
        ListenableFuture<MLModule> future = service.submit(new Callable<MLModule>() {
            public MLModule call() throws Exception {
                return deleteById(id);
            }
        });
        return future;
    }

    public MLModule save(MLModule module){

        if(module.getId()==null || module.getId().equals("")){
            module.setId(UUID.randomUUID().toString());
        }
        if(module.getCreated() == null){
            module.setCreated(new Date());
        }else{
            module.setUpdated(new Date());
        }

        if(module.getIsShell()){
            MLModule existing_module = findById(module.getId());

            if(existing_module != null) {
                existing_module.setModelSource(module.getModelSource().clone());
                existing_module.setTitle(module.getTitle());
                existing_module.setDescription(module.getDescription());
                existing_module.setCreated(module.getCreated());
                existing_module.setCreatedBy(module.getCreatedBy());
                existing_module.setUpdated(module.getUpdated());
                existing_module.setUpdatedBy(module.getUpdatedBy());

                Map<String, Double> attributes = module.getAttributes();
                for (String attrname : attributes.keySet()) {
                    existing_module.setAttribute(attrname, attributes.get(attrname));
                }

                existing_module.setProjectId(module.getProjectId());

                module = existing_module;
            }
        }


        return repository.save(module);
    }

    public MLModule create(MLProjectElementCreateCommand cmd) {

        MLModule module = MLModuleFactory.getInstance().create(cmd.getPrototype());
        module.setProjectId(cmd.getProjectId());
        module.setTitle(cmd.getTitle());
        module.setDescription(cmd.getDescription());
        module.setId(cmd.getId());
        save(module);
        return module;
    }

    public ListenableFuture<MLModule> createAsync(final MLProjectElementCreateCommand cmd) {
        ListenableFuture<MLModule> future = service.submit(() -> create(cmd));

        return future;
    }

    public MLModule batchUpdate(String moduleId, String batchId) {
        logger.info("batchUpdate("+moduleId+", "+batchId+") invoked!");

        MLModule module = repository.findById(moduleId);

        if(module != null) {
            logger.info("module: {}", JSON.toJSONString(module, SerializerFeature.BrowserCompatible));
        } else {
            logger.error("module not found: {}", moduleId);
        }



        IntelliContext batch = basicBatchRepository.findById(batchId);

        if(batch != null) {
            logger.info("batch: {}", JSON.toJSONString(batch, SerializerFeature.BrowserCompatible));
        } else {
            logger.error("batch not found: {}", batchId);
        }

        if(module == null || batch == null){
            return  null;
        }

        try {
            module.batchUpdate(batch);
        }catch(Exception ex){
            logger.error( "batchUpdate failed!", ex);
        }



        module = repository.save(module);

        return module;
    }

    public ListenableFuture<MLModule> batchUpdateAsync(final String moduleId, final String batchId) {
        ListenableFuture<MLModule> future = service.submit(() -> batchUpdate(moduleId, batchId));
        return future;
    }

    public IntelliContext batchPredict(String moduleId, String batchId) {
        MLModule module = repository.findById(moduleId);
        //logger.info("batchUpdate("+moduleId+", "+batchId+")");
        IntelliContext batch = basicBatchRepository.findById(batchId);

        //IntelliContext moduleBatch = basicBatchRepository.findById(module.getModelSourceId());

        //logger.info("batchPredict: batch: "+batch.getId()+", "+batch.getTitle()+", "+batch.size());
        //logger.info("batchPredict: moduleBatch: "+moduleBatch.getId()+", "+moduleBatch.getTitle()+", "+moduleBatch.size());
        //logger.info("batchPredict: module: "+module.getId()+", "+module.getTitle());


        doPredict(module, batch, null);

        batch = basicBatchRepository.save(batch);

        return batch;
    }

    private void doPredict(MLModule module, IntelliContext batch, MLTestCaseResult result){
        batch.setLabelPredictorId(module.getId());

        int m = batch.tupleCount();

        for(int i=0; i < m; ++i){
            batch.tupleAtIndex(i).setPredictedLabelOutput(null);
        }

        if(result != null) result.resultType = module.getOutputType();

        int anomalyCount = 0;


        String key_predictionAccuracy = "predictionAccuracy";




        try {

            if (module instanceof Classifier) {
                Classifier classifier = (Classifier) module;
                if(result != null) result.moduleType = "Classifier";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = batch.tupleAtIndex(i);
                    String predictedLabel = classifier.predict(tuple);
                    tuple.setPredictedLabelOutput(predictedLabel);
                }
                if(result != null) {
                    double predictionAccuracy = classifier.computePredictionAccuracy(batch);
                    result.attributes.put(key_predictionAccuracy, predictionAccuracy);
                }
            } else if (module instanceof BinaryClassifier) {
                BinaryClassifier classifier = (BinaryClassifier) module;
                if(result != null) result.moduleType = "BinaryClassifier";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = batch.tupleAtIndex(i);
                    String predictedLabel = classifier.isInClass(tuple, classifier.getModelSource()) ? classifier.getPositiveClassLabel() : classifier.getNegativeClassLabel();
                    tuple.setPredictedLabelOutput(predictedLabel);
                }
                if(result != null) {
                    double predictionAccuracy = classifier.computePredictionAccuracy(batch);
                    result.attributes.put(key_predictionAccuracy, predictionAccuracy);
                }
            } else if (module instanceof AnomalyDetector) {
                AnomalyDetector ad = (AnomalyDetector) module;
                if(result != null) result.moduleType = "AnomalyDetection";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = batch.tupleAtIndex(i);

                    boolean isOutlier = ad.isAnomaly(tuple);

                    tuple.setPredictedLabelOutput(isOutlier ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY);
                    anomalyCount += (isOutlier ? 1 : 0);
                }
                if(result != null) {
                    String key_anomalyRatio = "anomalyRatio";
                    result.attributes.put(key_anomalyRatio, m == 0 ? 0 : (double)anomalyCount / m);
                }
            } else if (module instanceof Clustering) {
                Clustering clustering = (Clustering) module;
                if(result != null) result.moduleType = "Clustering";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = batch.tupleAtIndex(i);
                    tuple.setPredictedLabelOutput(String.format("%d", clustering.getCluster(tuple)));
                }

                if(result != null) {
                    String key_DBI = "DBI";
                    double DBI = clustering.computeDBI(batch);
                    result.attributes.put(key_DBI, DBI);

                    int clusterCount = clustering.getClusterCount(batch);
                    result.attributes.put("clusterCount", (double)clusterCount);
                }
            } else if (module instanceof Regression) {
                Regression r = (Regression) module;
                if(result != null) result.moduleType = "Regression";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = batch.tupleAtIndex(i);
                    tuple.setPredictedNumericOutput(r.predict(tuple).predictedOutput);
                }
            } else {
                logger.warn("module cannot be casted");
            }

            if(module.getOutputType() != MLModuleOutputType.PredictedOutputs && module.isEvaluationSupported(batch)){
                for(int i = 0; i < batch.tupleCount(); ++i){
                    IntelliTuple tuple = batch.tupleAtIndex(i);
                    tuple.setPredictedNumericOutput(module.evaluate(batch.tupleAtIndex(i), batch));
                }
            }

        }catch (Exception ex){
            logger.error( "batchPredict failed!", ex);
        }
    }

    public ListenableFuture<IntelliContext> batchPredictAsync(final String moduleId, final String batchId) {
        ListenableFuture<IntelliContext> future = service.submit(() -> batchPredict(moduleId, batchId));
        return future;
    }

    public DataRow predict(Map<Integer, String> row, String moduleId) {
        MLModule module = repository.findById(moduleId);
        IntelliTuple tuple = module.newTuple(row);
        tuple = predict(tuple, module);
        return module.toDataRow(tuple);
    }

    public ListenableFuture<DataRow> predictAsync(final Map<Integer, String> tuple, final String moduleId) {
        ListenableFuture<DataRow> future = service.submit( () -> predict(tuple, moduleId));
        return future;
    }

    public DataRow predict(DataRow row, String moduleId) {
        MLModule module = repository.findById(moduleId);
        IntelliTuple tuple = module.newTuple(row);
        tuple = predict(tuple, module);
        return module.toDataRow(tuple);
    }

    private IntelliTuple predict(IntelliTuple tuple, MLModule module){
        if(module instanceof Classifier){
            Classifier classifier = (Classifier)module;
            classifier.predict(tuple);
        } else if (module instanceof BinaryClassifier) {
            BinaryClassifier classifier = (BinaryClassifier) module;
            String predictedLabel = classifier.isInClass(tuple, classifier.getModelSource()) ? classifier.getPositiveClassLabel() : classifier.getNegativeClassLabel();
            tuple.setPredictedLabelOutput(predictedLabel);

        } else if (module instanceof AnomalyDetector) {
            AnomalyDetector ad = (AnomalyDetector) module;
            boolean isOutlier = ad.isAnomaly(tuple);
            tuple.setPredictedLabelOutput(isOutlier ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY);

        } else if (module instanceof Clustering) {
            Clustering clustering = (Clustering) module;
            tuple.setPredictedLabelOutput(String.format("%d", clustering.getCluster(tuple)));
        } else if (module instanceof Regression) {
            Regression r = (Regression) module;
            tuple.setPredictedNumericOutput(r.predict(tuple).predictedOutput);
        } else {
            logger.warn("module cannot be casted");
        }
        return tuple;
    }

    public ListenableFuture<DataRow> predictAsync(final DataRow row, final String moduleId) {
        ListenableFuture<DataRow> future = service.submit( () -> predict(row, moduleId));
        return future;
    }

    public Collection<MLModuleOverview> findAllOverviewsByProjectId(String projectId){
        Collection<MLModule> modules = findAllByProjectId(projectId);

        List<MLModuleOverview> overviews = new ArrayList<MLModuleOverview>();

        for(MLModule module : modules){
            MLModuleOverview overview = new MLModuleOverview();
            overview.description = module.getDescription();
            overview.name = module.getTitle();
            overview.id = module.getId();
            overview.outputType = module.getOutputType();

            overviews.add(overview);
        }
        return overviews;
    }

    public ListenableFuture<Collection<MLModuleOverview>> findAllOverviewsByProjectIdAsync(final String projectId){
        ListenableFuture<Collection<MLModuleOverview>> future = service.submit(() -> findAllOverviewsByProjectId(projectId));

        return future;
    }

    public MLTestCaseResult batchTest(MLTestCase testCase){

        //logger.info("batchTest: batchId="+testCase.batchId);
        //logger.info("batchTest: moduleId="+testCase.moduleId);
        //logger.info("batchTest: testingModel="+testCase.testingModel);

        MLModule module = repository.findById(testCase.moduleId);
        IntelliContext batch = basicBatchRepository.findById(testCase.batchId);

        IntelliContext testingBatch = new IntelliContext(batch.getAttributeLevelSource());
        IntelliContext trainingBatch = new IntelliContext(batch.getAttributeLevelSource());

        TestingMode mode = testCase.testingModel.mode;

        if((module instanceof Clustering) || (module instanceof AnomalyDetector)){
            mode = TestingMode.TestOnTraining;
        }

        MLTestCaseResult result = new MLTestCaseResult();
        result.batchId = testCase.batchId;
        result.moduleId = testCase.moduleId;
        result.moduleName = testCase.moduleName;
        result.batchName = testCase.batchName;

        try {

            if (mode == TestingMode.NoTraining) {
                testingBatch = batch;
                trainingBatch = batch;
                doPredict(module, testingBatch, result);
            } else if (mode == TestingMode.CrossValidation) {

                int p = testCase.testingModel.crossValidationLeaveOutCount;
                int n = testCase.testingModel.crossValidationFoldCount;



                int m = batch.tupleCount();
                int foldSize = (int)(Math.floor((double)m / n));

                for(int i=0; i < n; ++i){ // simplified cross validation running n times instead of (n! / (n-p)!p!)
                    trainingBatch.clearTuples();
                    testingBatch.clearTuples();
                    for(int j=0; j < p; ++i){
                        for(int k=0; k < foldSize; ++k){
                            int index = i * foldSize + j * foldSize + k;
                            index = index % m;
                            trainingBatch.add(batch.tupleAtIndex(index));
                        }
                    }
                    for(int j=p; j < n; ++j){
                        for(int k=0; k < foldSize; ++k){
                            int index = i * foldSize + j * foldSize + k;
                            index = index % m;
                            testingBatch.add(batch.tupleAtIndex(index));
                        }
                    }

                    MLTestCaseResult result2 = new MLTestCaseResult();
                    result2.batchId = testCase.batchId;
                    result2.moduleId = testCase.moduleId;
                    result2.moduleName = testCase.moduleName;
                    result2.batchName = testCase.batchName;

                    module.batchUpdate(trainingBatch);
                    doPredict(module, testingBatch, result2);

                    for(String attrname : result2.attributes.keySet()){
                        double attrvalue = result2.attributes.get(attrname);
                        if(result.attributes.containsKey(attrname)){
                            result.attributes.put(attrname, result.attributes.get(attrname) + attrvalue);
                        }else{
                            result.attributes.put(attrname, attrvalue);
                        }
                    }

                    List<String> attrnames = new ArrayList<>();
                    for(String attrname: result.attributes.keySet()){
                        attrnames.add(attrname);
                    }

                    for(String attrname : attrnames){
                        double attrvalue = result.attributes.get(attrname);
                        attrvalue /= n;
                        result.attributes.put(attrname, attrvalue);
                    }
                }




            } else {
                if (mode == TestingMode.TestOnTraining) {
                    trainingBatch = batch;
                    testingBatch = batch;
                } else if (mode == TestingMode.ByTrainingPortion) {
                    int trainingSize = (int) (Math.floor(batch.tupleCount() * testCase.testingModel.trainingPortion));
                    for (int i = 0; i < trainingSize; ++i) {
                        trainingBatch.add(batch.tupleAtIndex(i));
                    }
                    for (int i = trainingSize; i < batch.tupleCount(); ++i) {
                        testingBatch.add(batch.tupleAtIndex(i));
                    }
                }

                module.batchUpdate(trainingBatch);
                doPredict(module, testingBatch, result);
            }
        }catch(Exception ex){
            logger.error( "batchTest failed", ex);
        }

        return result;

    }

    public ListenableFuture<MLTestCaseResult> batchTestAsync(final MLTestCase testCase){
        ListenableFuture<MLTestCaseResult> future = service.submit(() -> batchTest(testCase));
        return future;
    }

}
