package com.github.chen0040.ml.svm.tests.anomaly.supervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.svm.anomaly.supervised.BiClassSVMAnomalyDetector;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.sk.utils.StringHelper;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by memeanalytics on 14/8/15.
 */
public class AnomalySMOSVCTest {
    @Test
    public void testFindOutliers(){
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_outliers = { "outliers1.txt", "outliers2.txt"};
        double[] thresholds = { 9.06576994861594E-5, 1.7538486542151838E-18 };

        for(int k=0; k < filenames_X.length; ++k){

            String filename_X = filenames_X[k];
            String filename_outliers = filenames_outliers[k];
            double threshold = thresholds[k];

            IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);
            final List<Integer> expected_outliers = CSVService.getInstance().readIntegers(FileUtils.getResourceFile(filename_outliers), " ", false);
            for(int i=0; i < expected_outliers.size(); ++i){
                expected_outliers.set(i, expected_outliers.get(i)-1);
            }

            for(int i=0; i < expected_outliers.size(); ++i){
                int tupleIndex = expected_outliers.get(i);
                trainingBatch.tupleAtIndex(tupleIndex).setLabelOutput(AnomalyClassLabels.IS_ANOMALY);
            }

            BiClassSVMAnomalyDetector algorithm = new BiClassSVMAnomalyDetector();
            algorithm.batchUpdate(trainingBatch);

            List<Integer> predicted_outliers = algorithm.findOutlierPositions(trainingBatch);


            System.out.println("Expected Outliers:"+expected_outliers.size());
            System.out.println(StringHelper.toString(expected_outliers));

            System.out.println("Predicted Outliers:"+predicted_outliers.size());
            System.out.println(StringHelper.toString(predicted_outliers));

            System.out.println("F1 Score: "+ F1Score.score(expected_outliers, predicted_outliers, trainingBatch.tupleCount()));
        }
    }
}
