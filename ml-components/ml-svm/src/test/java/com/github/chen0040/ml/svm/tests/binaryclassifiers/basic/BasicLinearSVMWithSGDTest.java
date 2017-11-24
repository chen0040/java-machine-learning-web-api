package com.github.chen0040.ml.svm.tests.binaryclassifiers.basic;

import com.github.chen0040.ml.commons.classifiers.F1ScoreResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.classifiers.PredictionScore;
import com.github.chen0040.sk.dom.basic.DomFileInfo;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.ml.svm.sgd.LinearSVCWithSGD;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * Created by memeanalytics on 14/8/15.
 */
public class BasicLinearSVMWithSGDTest {
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

        LinearSVCWithSGD algorithm = new LinearSVCWithSGD("1");
        algorithm.setC(1);

        algorithm.batchUpdate(trainingBatch);

        double score = PredictionScore.score(algorithm, trainingBatch, true);
        System.out.println("Prediction Accuracy: "+score);

        F1ScoreResult f1score = F1Score.score(algorithm, trainingBatch);
        System.out.println(f1score);

        //assertTrue(score > 0.99);
        //assertTrue(f1score.getPrecision() > 0.99);
        //assertTrue(f1score.getRecall() > 0.99);
        //assertTrue(f1score.getF1Score() > 0.99);
    }

}
