package com.github.chen0040.ml.web.tests;

import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.sdk.services.MLModuleFactory;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLModuleFactoryTest {
    @Test
    public void testListModules(){

        System.out.println("Total module count: "+ MLModuleFactory.getInstance().getModuleCount());

        int index;

        index = 1;
        System.out.println("Binary Classifiers: ");
        Collection<String> biClassifiers = MLModuleFactory.getInstance().getBinaryClassifiers();
        for(String biClassifier : biClassifiers){
            System.out.println(index + ":\t" + biClassifier);
            MLModule module = MLModuleFactory.getInstance().create(biClassifier);
            assertNotNull(module);
            index++;
        }

        System.out.println();

        index = 1;
        System.out.println("Classifiers: ");
        Collection<String> classifiers = MLModuleFactory.getInstance().getClassifiers();
        for(String classifier : classifiers){
            System.out.println(index+":\t"+classifier);
            MLModule module = MLModuleFactory.getInstance().create(classifier);
            assertNotNull(module);
            index++;
        }

        System.out.println();

        index = 1;
        System.out.println("Anomaly Detectors: ");
        Collection<String> anomalyDetectors = MLModuleFactory.getInstance().getAnomalyDetectors();
        for(String ad : anomalyDetectors){
            System.out.println(index+":\t"+ad);
            MLModule module = MLModuleFactory.getInstance().create(ad);
            assertNotNull(module);
            index++;
        }

        System.out.println();

        index = 1;
        System.out.println("Regressions: ");
        Collection<String> regressions = MLModuleFactory.getInstance().getRegressions();
        for(String regression : regressions){
            System.out.println(index+":\t"+regression);
            MLModule module = MLModuleFactory.getInstance().create(regression);
            assertNotNull(module);
            index++;
        }

        System.out.println();

        index = 1;
        System.out.println("Clustering: ");
        Collection<String> clusterings = MLModuleFactory.getInstance().getClusterings();
        for(String clustering : clusterings){
            System.out.println(index+":\t"+clustering);
            MLModule module = MLModuleFactory.getInstance().create(clustering);
            assertNotNull(module);
            index++;
        }
    }
}
