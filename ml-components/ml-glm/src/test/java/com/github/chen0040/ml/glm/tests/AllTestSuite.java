package com.github.chen0040.ml.glm.tests;

import com.github.chen0040.ml.glm.tests.anomaly.TestLogisticAnomalyDetector;
import com.github.chen0040.ml.glm.tests.logistic.TestGlmIrlsLogistic;
import com.github.chen0040.ml.glm.tests.binaryclassifiers.TestLogisticBinaryClassifier;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by memeanalytics on 25/8/15.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestLogisticAnomalyDetector.class, TestLogisticBinaryClassifier.class, TestGlmIrlsLogistic.class})
public class AllTestSuite {
}
