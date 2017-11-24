package com.github.chen0040.ml.sdk.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.ann.art.classifiers.ARTMAPClassifier;
import com.github.chen0040.ml.ann.art.clustering.ART1Clustering;
import com.github.chen0040.ml.ann.art.clustering.FuzzyARTClustering;
import com.github.chen0040.ml.ann.kohonen.clustering.SOFM;
import com.github.chen0040.ml.ann.mlp.classifiers.MLPClassifier;
import com.github.chen0040.ml.arm.apriori.Apriori;
import com.github.chen0040.ml.bayes.nbc.NBC;
import com.github.chen0040.ml.clustering.dbscan.DBSCAN;
import com.github.chen0040.ml.clustering.em.EMClustering;
import com.github.chen0040.ml.clustering.hierarchical.HierarchicalClustering;
import com.github.chen0040.ml.clustering.kmeans.KMeans;
import com.github.chen0040.ml.clustering.onelink.SingleLinkageClustering;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.arm.AssocRuleMiner;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifier;
import com.github.chen0040.ml.commons.clustering.Clustering;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.regressions.Regression;
import com.github.chen0040.ml.gaussianmixture.MultiVariateNormalOutliers;
import com.github.chen0040.ml.gaussianmixture.NormalOutliers;
import com.github.chen0040.ml.gaussianmixture.RANSAC;
import com.github.chen0040.ml.glm.anomaly.LogisticAnomalyDetector;
import com.github.chen0040.ml.glm.binaryclassifiers.LogisticBinaryClassifier;
import com.github.chen0040.ml.glm.classifiers.OneVsOneLogisticClassifier;
import com.github.chen0040.ml.glm.classifiers.OneVsRestLogisticClassifier;
import com.github.chen0040.ml.glm.linear.GlmLinearModel;
import com.github.chen0040.ml.glm.logistic.LogisticModel;
import com.github.chen0040.ml.glm.regression.GlmRegression;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import com.github.chen0040.ml.knn.anomaly.KNNDistanceAnomalyDetector;
import com.github.chen0040.ml.knn.anomaly.ORCA;
import com.github.chen0040.ml.knn.classifiers.KNNClassifier;
import com.github.chen0040.ml.lof.CBLOF;
import com.github.chen0040.ml.lof.LDOF;
import com.github.chen0040.ml.lof.LOCI;
import com.github.chen0040.ml.lof.LOF;
import com.github.chen0040.ml.svm.anomaly.unsupervised.OneClassSVMAnomalyDetector;
import com.github.chen0040.ml.svm.binaryclassifiers.SVC;
import com.github.chen0040.ml.svm.classifiers.OneVsOneSVC;
import com.github.chen0040.ml.svm.classifiers.OneVsRestSVC;
import com.github.chen0040.ml.svm.regression.SVR;
import com.github.chen0040.ml.trees.id3.ID3;
import com.github.chen0040.ml.trees.iforest.IsolationForest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.github.chen0040.ml.commons.anomaly.AnomalyDetector;
import com.github.chen0040.ml.commons.classifiers.Classifier;
import com.github.chen0040.ml.svm.sgd.LinearSVCWithSGD;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLModuleFactory {
    private Set<String> binaryClassifiers;
    private Set<String> clusterings;
    private Set<String> classifiers;
    private Set<String> anomalyDetectors;
    private Set<String> regressions;
    private Set<String> arms;
    private ListeningExecutorService service;

    public int getModuleCount(){
        return binaryClassifiers.size() + clusterings.size() + classifiers.size()
                + anomalyDetectors.size() + regressions.size() + arms.size();
    }

    public List<String> getAllPrototypes(){
        List<String> prototypes = new ArrayList<>();
        prototypes.addAll(binaryClassifiers);
        prototypes.addAll(classifiers);
        prototypes.addAll(clusterings);
        prototypes.addAll(anomalyDetectors);
        prototypes.addAll(regressions);
        prototypes.addAll(arms);
        return prototypes;
    }

    public Map<String, List<String>> getPrototypeMap(){
        Map<String, List<String>> hmap = new HashMap<>();
        hmap.put("BinaryClassifier", new ArrayList<>(binaryClassifiers));
        hmap.put("Classifier", new ArrayList<>(classifiers));
        hmap.put("Clustering", new ArrayList<>(clusterings));
        hmap.put("AnomalyDetection", new ArrayList<>(anomalyDetectors));
        hmap.put("ARM", new ArrayList<>(arms));
        hmap.put("Regression", new ArrayList<>(regressions));
        return hmap;
    }

    public ListenableFuture<Map<String, List<String>>> getPrototypeMapAsync(){
        ListenableFuture<Map<String, List<String>>> future = service.submit(() -> getPrototypeMap());
        return future;
    }

    public Set<String> getAnomalyDetectors() {
        return anomalyDetectors;
    }

    public Set<String> getRegressions() {
        return regressions;
    }

    public Set<String> getClusterings() {
        return clusterings;
    }

    private static class Holder{
        static MLModuleFactory instance = new MLModuleFactory();
    }

    public static MLModuleFactory getInstance(){
        return Holder.instance;
    }

    private MLModuleFactory(){
        binaryClassifiers = new ConcurrentSkipListSet<>();
        clusterings = new ConcurrentSkipListSet<>();
        anomalyDetectors = new ConcurrentSkipListSet<>();
        classifiers = new ConcurrentSkipListSet<>();
        regressions = new ConcurrentSkipListSet<>();
        arms = new ConcurrentSkipListSet<>();
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));


        hook(ARTMAPClassifier.class);
        hook(ART1Clustering.class);
        hook(FuzzyARTClustering.class);
        hook(SOFM.class);
        hook(MLPClassifier.class);

        hook(DBSCAN.class);
        hook(EMClustering.class);
        hook(HierarchicalClustering.class);
        hook(KMeans.class);
        hook(SingleLinkageClustering.class);

        hook(NBC.class);

        hook(Apriori.class);

        hook(MultiVariateNormalOutliers.class);
        hook(NormalOutliers.class);
        hook(RANSAC.class);

        hook(LogisticAnomalyDetector.class);
        hook(LogisticBinaryClassifier.class);
        hook(OneVsOneLogisticClassifier.class);
        hook(OneVsRestLogisticClassifier.class);
        hook(GlmRegression.class);
        hook(GlmLinearModel.class);
        hook(LogisticModel.class);
        hook(GlmSolver.class);

        hook(KNNDistanceAnomalyDetector.class);
        hook(ORCA.class);
        hook(KNNClassifier.class);

        hook(CBLOF.class);
        hook(LDOF.class);
        hook(LOCI.class);
        hook(LOF.class);

        hook(OneClassSVMAnomalyDetector.class);
        hook(SVC.class);
        hook(OneVsOneSVC.class);
        hook(OneVsRestSVC.class);
        hook(LinearSVCWithSGD.class);
        hook(SVR.class);

        hook(ID3.class);
        hook(IsolationForest.class);



    }

    public Collection<String> getBinaryClassifiers(){
        return binaryClassifiers;
    }

    public Collection<String> getClassifiers(){
        return classifiers;
    }

    public void hook(Class<?> clazz){

        if(Modifier.isAbstract(clazz.getModifiers())) return;

        String className = clazz.getCanonicalName();
        while(clazz.getSuperclass() != null){
            Class<?>[] interfaces = clazz.getInterfaces();
            for(Class<?> iface : interfaces) {
                if (iface.equals(AnomalyDetector.class)) {
                    anomalyDetectors.add(className);
                } else if (iface.equals(BinaryClassifier.class)) {
                    binaryClassifiers.add(className);
                } else if (iface.equals(Clustering.class)) {
                    clusterings.add(className);
                } else if (iface.equals(Regression.class)) {
                    regressions.add(className);
                } else if (iface.equals(Classifier.class)) {
                    classifiers.add(className);
                } else if (iface.equals(AssocRuleMiner.class)) {
                    arms.add(className);
                }
            }
            clazz=clazz.getSuperclass();
        }
    }

    public MLModule convert(Map<String, Object> data){
        String className = (String)data.get("prototype");

        AttributeValueDiscretizer avd = null;
        if(data.containsKey("inputDiscretizer")){
            Map<String, Object> dmap = (Map<String, Object>)data.get("inputDiscretizer");
            String className2 = (String)dmap.get("prototype");
            Class<?> clazz2;
            try {
                clazz2 = Class.forName(className2);
                avd = (AttributeValueDiscretizer)JSON.parseObject(JSON.toJSONString(dmap, SerializerFeature.BrowserCompatible), clazz2);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        Class<?> clazz = null;
        MLModule object = null;
        try {
            clazz = Class.forName(className);
            object = (MLModule)JSON.parseObject(JSON.toJSONString(data, SerializerFeature.BrowserCompatible), clazz);

            if(avd != null) {
                object.setInputDiscretizer(avd);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;

    }

    public MLModule create(String className){
        Object obj = null;
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor();
            obj = ctor.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return (MLModule)obj;
    }
}
