package com.github.chen0040.ml.sdk.models.workflows;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.MLModuleOutputType;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.anomaly.AnomalyDetector;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.commons.classifiers.Classifier;
import com.github.chen0040.ml.commons.clustering.Clustering;
import com.github.chen0040.ml.commons.regressions.Regression;
import com.github.chen0040.ml.sdk.models.MLTestCaseResult;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 9/8/15.
 */
public class MLWorkflow {
    public ArrayList<MLWorkflowNode> nodes = new ArrayList<MLWorkflowNode>();
    public ArrayList<MLWorkflowConnection> connections = new ArrayList<MLWorkflowConnection>();

    private static final Logger logger = Logger.getLogger(String.valueOf(MLWorkflow.class));

    public MLWorkflow clone(){
        MLWorkflow clone = new MLWorkflow();
        for(int i=0; i < nodes.size(); ++i){
            clone.nodes.add(nodes.get(i).clone());
        }
        for(int i=0; i < connections.size(); ++i){
            clone.connections.add(connections.get(i).clone());
        }

        return clone;
    }

    public List<MLWorkflowConnection> findInputConnections(MLWorkflowNode node){
        List<MLWorkflowConnection> inputConnections = new ArrayList<MLWorkflowConnection>();
        for(int i=0; i < connections.size(); ++i){
            MLWorkflowConnection c = connections.get(i);
            if(c.dest.nodeID == node.id){
                inputConnections.add(c);
            }
        }

        return inputConnections;
    }

    public List<MLWorkflowConnection> findOutputConnections(MLWorkflowNode node){
        List<MLWorkflowConnection> outputConnections = new ArrayList<MLWorkflowConnection>();
        for(int i=0; i < connections.size(); ++i){
            MLWorkflowConnection c = connections.get(i);
            if(c.source.nodeID == node.id){
                outputConnections.add(c);
            }
        }

        return outputConnections;
    }

    public List<MLWorkflowNode> findBatchSources(){
        List<MLWorkflowNode> sources = new ArrayList<MLWorkflowNode>();

        for(int i=0; i < nodes.size(); ++i){
            MLWorkflowNode node = nodes.get(i);
            if(node.modelType == MLWorkflowNodeModelType.Batch) {
                List<MLWorkflowConnection> inputConnections = findInputConnections(node);
                if (inputConnections.size() == 0) {
                    sources.add(node);
                }
            }
        }

        logger.info("batch source count: "+sources.size());
        for(int i=0; i < sources.size(); ++i){
            logger.info("batch source: "+sources.get(i).name);
        }

        return sources;
    }

    private IntelliContext findBatch(MLWorkflowNode node, Function<String, IntelliContext> findBatchById){
        if(node.modelType == MLWorkflowNodeModelType.Batch){
            return findBatchById.apply(node.modelId);
        }
        return null;
    }


    public void processNode(Function<String, MLModule> findModuleById, Function<String, IntelliContext> findBatchById, Function<MLModule, MLModule> moduleStore, Function<IntelliContext, IntelliContext> batchStore){

        List<MLWorkflowNode> sources = findBatchSources();


        HashSet<MLWorkflowNode> visited = new HashSet<MLWorkflowNode>();
        Queue<MLWorkflowNode> queue = new LinkedList<MLWorkflowNode>();

        for(int i=0; i < sources.size(); ++i){
            MLWorkflowNode source = sources.get(i);
            processNode(source, visited, queue, findModuleById, findBatchById, moduleStore, batchStore);
        }

        while(!queue.isEmpty()){
            MLWorkflowNode node = queue.remove();
            processNode(node, visited, queue, findModuleById, findBatchById, moduleStore, batchStore);
        }
    }

    private IntelliContext findTrainingInputBatch(MLWorkflowNode node, Function<String, IntelliContext> findBatchById){
        String pinName = "Train-In";
        MLWorkflowNode trainingInputNode = findInputNodeConnectedToPin(node, pinName);
        if(trainingInputNode == null) return null;
        return findBatch(trainingInputNode, findBatchById);
    }

    private IntelliContext findPredictionInputBatch(MLWorkflowNode node, Function<String, IntelliContext> findBatchById){
        String pinName = "Predict-In";
        MLWorkflowNode predictionInputNode = findInputNodeConnectedToPin(node, pinName);
        if(predictionInputNode == null) return null;
        return findBatch(predictionInputNode, findBatchById);
    }

    private IntelliContext findPredictionOutputBatch(MLWorkflowNode node, Function<String, IntelliContext> findBatchById){
        String pinName = "Predict-Out";
        MLWorkflowNode predictionOutputNode = findOutputNodeConnectedToPin(node, pinName);
        if(predictionOutputNode == null) return null;
        return findBatch(predictionOutputNode, findBatchById);
    }

    private MLWorkflowNode findNodeById(int nodeId){
        for(int i=0; i < nodes.size(); ++i){
            MLWorkflowNode node = nodes.get(i);
            if(node.id == nodeId){
                return node;
            }
        }

        return null;
    }

    private MLWorkflowNode findInputNodeConnectedToPin(MLWorkflowNode node, String selectedPinName){
        MLWorkflowConnection c = findInputConnectionAtPin(node, selectedPinName);
        if(c == null) return null;
        return findNodeById(c.source.nodeID);
    }

    private MLWorkflowConnection findInputConnectionAtPin(MLWorkflowNode node, String selectedPinName){
        List<MLWorkflowConnection> inputConnetions = findInputConnections(node);

        MLWorkflowConnection selectedConnection = null;
        for(int i=0; i < inputConnetions.size(); ++i){
            MLWorkflowConnection c = inputConnetions.get(i);
            String pinName = node.inputConnectors.get(c.dest.connectorIndex).name;
            if(pinName.equals(selectedPinName)){
                selectedConnection = c;
                break;
            }
        }

        return selectedConnection;
    }

    private MLWorkflowNode findOutputNodeConnectedToPin(MLWorkflowNode node, String selectedPinName){
        MLWorkflowConnection c = findOutputConnectionAtPin(node, selectedPinName);
        if(c == null) return null;
        return findNodeById(c.dest.nodeID);
    }

    private MLWorkflowConnection findOutputConnectionAtPin(MLWorkflowNode node, String selectedPinName){
        List<MLWorkflowConnection> outputConnetions = findOutputConnections(node);

        MLWorkflowConnection selectedConnection = null;
        for(int i=0; i < outputConnetions.size(); ++i){
            MLWorkflowConnection c = outputConnetions.get(i);
            String pinName = node.outputConnectors.get(c.source.connectorIndex).name;
            if(pinName.equals(selectedPinName)){
                selectedConnection = c;
                break;
            }
        }

        return selectedConnection;
    }

    private void processNode(MLWorkflowNode node, HashSet<MLWorkflowNode> visited, Queue<MLWorkflowNode> queue, Function<String, MLModule> findModuleById, Function<String, IntelliContext> findBatchById, Function<MLModule, MLModule> moduleStore, Function<IntelliContext, IntelliContext> batchStore){
        if(node.modelType == MLWorkflowNodeModelType.Module){

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            MLModule module = findModuleById.apply(node.modelId);

            IntelliContext training_input = findTrainingInputBatch(node, findBatchById);

            if(training_input != null) {
                module.batchUpdate(training_input);
                module = moduleStore.apply(module);
            } else{
                logger.warning("Workflow failed to find the source for training at module "+node.name);
            }

            IntelliContext prediction_input = findPredictionInputBatch(node, findBatchById);

            IntelliContext prediction_output = findPredictionOutputBatch(node, findBatchById);

            if(prediction_input != null && training_input != null && prediction_output != null) {
                doPredict(module, training_input, prediction_input, prediction_output, null);
                prediction_output.setDescription("This is a predicted output from \""+training_input.getTitle()+"\" by algorithm \""+module.getTitle()+"\" at "+fmt.format(new Date()));
                prediction_output = batchStore.apply(prediction_output);
            } else{
                if(prediction_input == null){
                    logger.warning("Workflow failed to find the prediction input at module "+node.name);
                }
                if(prediction_output == null){
                    logger.warning("Workflow failed to find the prediction output at module "+node.name);
                }
            }
        }
        List<MLWorkflowConnection> outputConnections = findOutputConnections(node);

        for(int i=0; i < outputConnections.size(); ++i){
            MLWorkflowConnection c = outputConnections.get(i);
            MLWorkflowNode downstreamNode = findNodeById(c.dest.nodeID);
            if(!visited.contains(downstreamNode)){
                visited.add(downstreamNode);
                queue.add(downstreamNode);
            }
        }
    }

    private void doPredict(MLModule module, IntelliContext trainingInputBatch, IntelliContext predictionInputBatch, IntelliContext predictionOutputBatch, MLTestCaseResult result) {
        predictionOutputBatch.setLabelPredictorId(module.getId());
        predictionOutputBatch.clearTuples();

        if (result != null) result.resultType = module.getOutputType();

        int anomalyCount = 0;


        String key_predictionAccuracy = "predictionAccuracy";

        int m = predictionInputBatch.tupleCount();
        try {

            if (module instanceof Classifier) {
                Classifier classifier = (Classifier) module;
                if (result != null) result.moduleType = "Classifier";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = (IntelliTuple)((IntelliTuple)predictionInputBatch.tupleAtIndex(i)).clone();
                    String predictedLabel = classifier.predict(tuple);
                    tuple.setPredictedLabelOutput(predictedLabel);
                    predictionOutputBatch.add(tuple);
                }
                if (result != null) {
                    double predictionAccuracy = classifier.computePredictionAccuracy(predictionInputBatch);
                    result.attributes.put(key_predictionAccuracy, predictionAccuracy);
                }
            } else if (module instanceof BinaryClassifier) {
                BinaryClassifier classifier = (BinaryClassifier) module;
                if (result != null) result.moduleType = "BinaryClassifier";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = (IntelliTuple)((IntelliTuple)predictionInputBatch.tupleAtIndex(i)).clone();
                    String predictedLabel = classifier.isInClass(tuple, classifier.getModelSource()) ? classifier.getPositiveClassLabel() : classifier.getNegativeClassLabel();
                    tuple.setPredictedLabelOutput(predictedLabel);
                    predictionOutputBatch.add(tuple);
                }
                if (result != null) {
                    double predictionAccuracy = classifier.computePredictionAccuracy(predictionInputBatch);
                    result.attributes.put(key_predictionAccuracy, predictionAccuracy);
                }
            } else if (module instanceof AnomalyDetector) {
                AnomalyDetector ad = (AnomalyDetector) module;
                if (result != null) result.moduleType = "AnomalyDetection";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = (IntelliTuple)((IntelliTuple)predictionInputBatch.tupleAtIndex(i)).clone();

                    boolean isOutlier = ad.isAnomaly(tuple);

                    tuple.setPredictedLabelOutput(isOutlier ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY);
                    anomalyCount += (isOutlier ? 1 : 0);

                    predictionOutputBatch.add(tuple);
                }
                if (result != null) {
                    String key_anomalyRatio = "anomalyRatio";
                    result.attributes.put(key_anomalyRatio, m == 0 ? 0 : (double) anomalyCount / m);
                }
            } else if (module instanceof Clustering) {
                Clustering clustering = (Clustering) module;
                if (result != null) result.moduleType = "Clustering";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = (IntelliTuple)((IntelliTuple)predictionInputBatch.tupleAtIndex(i)).clone();
                    tuple.setPredictedLabelOutput(String.format("%d", clustering.getCluster(tuple)));
                    predictionOutputBatch.add(tuple);
                }

                if (result != null) {
                    String key_DBI = "DBI";
                    double DBI = clustering.computeDBI(predictionInputBatch);
                    result.attributes.put(key_DBI, DBI);

                    int clusterCount = clustering.getClusterCount(predictionInputBatch);
                    result.attributes.put("clusterCount", (double) clusterCount);
                }
            } else if (module instanceof Regression) {
                Regression r = (Regression) module;
                if (result != null) result.moduleType = "Regression";
                for (int i = 0; i < m; ++i) {
                    IntelliTuple tuple = (IntelliTuple)((IntelliTuple)predictionInputBatch.tupleAtIndex(i)).clone();
                    tuple.setPredictedNumericOutput(r.predict(tuple).predictedOutput);
                    predictionOutputBatch.add(tuple);
                }
            } else {
                logger.warning("module cannot be casted");
            }

            if (module.getOutputType() != MLModuleOutputType.PredictedOutputs && module.isEvaluationSupported(predictionOutputBatch)) {
                for (int i = 0; i < predictionOutputBatch.tupleCount(); ++i) {
                    IntelliTuple tuple = predictionOutputBatch.tupleAtIndex(i);
                    tuple.setPredictedNumericOutput(module.evaluate(predictionInputBatch.tupleAtIndex(i), module.getModelSource()));
                }
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "doPredict failed!", ex);
        }
    }
}
