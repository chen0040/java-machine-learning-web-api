package com.github.chen0040.ml.svm.tests.binaryclassifiers.basic;

import com.github.chen0040.ml.commons.classifiers.F1ScoreResult;
import com.github.chen0040.ml.commons.classifiers.PredictionScore;
import com.github.chen0040.ml.svm.smo.SMOSVC;
import com.github.chen0040.ml.svm.smo.models.SMOSVMModel;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.sk.dom.basic.DomFileInfo;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.ml.svm.smo.kernels.GaussianKernelFunction;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Created by memeanalytics on 13/8/15.
 */
public class BasicSMOSVCTest {
    @Test
    public void testLinearKernel(){
        DomFileInfo fileInfo = CSVService.getInstance().getFileInfo(FileUtils.getResourceFile("data1X.csv"), ",", false, null);
        int dimension = fileInfo.getColCount();

        IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile("data1X.csv"), ",", false);
        List<String> trainingLabels = CSVService.getInstance().readStrings(FileUtils.getResourceFile("data1y.csv"), ",", false);

        assertEquals(trainingBatch.tupleCount(), trainingLabels.size());

        for(int i = 0; i < trainingBatch.tupleCount(); ++i){
            trainingBatch.tupleAtIndex(i).setLabelOutput(trainingLabels.get(i));
        }

        SMOSVC algorithm = new SMOSVC("1");
        algorithm.setC(100);
        algorithm.setTol(1e-3);
        algorithm.setMaxPasses(20);

        algorithm.batchUpdate(trainingBatch);

        SMOSVMModel model = algorithm.model();
        System.out.println(model);

        double score = PredictionScore.score(algorithm, trainingBatch, true);
        System.out.println("Prediction Accuracy: "+score);

        F1ScoreResult f1score = F1Score.score(algorithm, trainingBatch);
        System.out.println(f1score);

        assertTrue(score > 0.95);
        assertTrue(f1score.getPrecision() > 0.95);
        assertTrue(f1score.getRecall() > 0.95);
        assertTrue(f1score.getF1Score() > 0.95);
    }

    @Test
    public void testGaussianKernel(){
        IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile("data2X.csv"), ",", false);
        List<String> trainingLabels = CSVService.getInstance().readStrings(FileUtils.getResourceFile("data2y.csv"), ",", false);

        assertEquals(trainingBatch.tupleCount(), trainingLabels.size());

        for(int i = 0; i < trainingBatch.tupleCount(); ++i){
            trainingBatch.tupleAtIndex(i).setLabelOutput(trainingLabels.get(i));
        }

        SMOSVC algorithm = new SMOSVC("1");
        algorithm.setC(1);
        algorithm.setTol(1e-3);
        //algorithm.setMaxPasses(20);
        algorithm.setKernelFunction(new GaussianKernelFunction(0.1));

        algorithm.batchUpdate(trainingBatch);

        SMOSVMModel model = algorithm.model();
        System.out.println(model);

        double score = PredictionScore.score(algorithm, trainingBatch, true);
        System.out.println("Prediction Accuracy: "+score);

        F1ScoreResult f1score = F1Score.score(algorithm, trainingBatch);
        System.out.println(f1score);

        assertTrue(score > 0.95);
        assertTrue(f1score.getPrecision() > 0.95);
        assertTrue(f1score.getRecall() > 0.95);
        assertTrue(f1score.getF1Score() > 0.95);
    }
}
