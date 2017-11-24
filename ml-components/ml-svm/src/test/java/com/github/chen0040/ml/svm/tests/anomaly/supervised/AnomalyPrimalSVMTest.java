package com.github.chen0040.ml.svm.tests.anomaly.supervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.svm.anomaly.supervised.BiClassSVMAnomalyDetector;
import com.github.chen0040.ml.svm.sgd.LinearSVCWithSGD;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import com.github.chen0040.sk.dom.basic.DomElement;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.sk.utils.StringHelper;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;


/**
 * Created by memeanalytics on 12/8/15.
 */
public class AnomalyPrimalSVMTest {



    @Test
    public void testCalcProbability() {
        String[] filenames_Xval = {"Xval1.txt" }; //, "Xval2.txt"};
        String[] filenames_yval = { "yval1.txt"}; // "yval2.txt"};


        for (int k = 0; k < filenames_Xval.length; ++k) {
            String filename_Xval = filenames_Xval[k];
            String filename_yval = filenames_yval[k];

            IntelliContext traingBatch = new IntelliContext(FileUtils.getResourceFile(filename_Xval), " ", false);


            LinearSVCWithSGD algorithm = new LinearSVCWithSGD(AnomalyClassLabels.IS_ANOMALY);
            algorithm.setC(100);


            final List<String> trainingLabels = new ArrayList<>();
            CSVService.getInstance().readDoc(FileUtils.getResourceFile(filename_yval), null, false, new Function<DomElement, Boolean>() {
                public Boolean apply(DomElement line) {
                    String[] values = line.data;
                    assertEquals(1, values.length);
                    boolean is_anomaly = StringHelper.parseBoolean(values[0]);
                    if (is_anomaly) trainingLabels.add(AnomalyClassLabels.IS_ANOMALY);
                    else trainingLabels.add(AnomalyClassLabels.IS_NOT_ANOMALY);
                    return true;
                }
            }, null);

            assertEquals(traingBatch.tupleCount(), trainingLabels.size());

            for(int i = 0; i < traingBatch.tupleCount(); ++i){
                traingBatch.tupleAtIndex(i).setLabelOutput(trainingLabels.get(i));
            }


            algorithm.batchUpdate(traingBatch);

            for (int i = 0; i < traingBatch.tupleCount(); ++i) {
                double p_predicted = algorithm.evaluate(traingBatch.tupleAtIndex(i), traingBatch);
                System.out.println(String.format("p = %.3f label %s", p_predicted, traingBatch.tupleAtIndex(i).getLabelOutput()));
            }
        }

    }

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
                trainingBatch.tupleAtIndex(expected_outliers.get(i)).setLabelOutput(AnomalyClassLabels.IS_ANOMALY);
            }

            BiClassSVMAnomalyDetector algorithm = new BiClassSVMAnomalyDetector();
            algorithm.setClassifier(new LinearSVCWithSGD(AnomalyClassLabels.IS_ANOMALY));
            algorithm.batchUpdate(trainingBatch);

            List<Integer> predicted_outliers = algorithm.findOutlierPositions(trainingBatch);

            System.out.println("Expected Outliers:"+expected_outliers.size());
            System.out.println(StringHelper.toString(expected_outliers));

            System.out.println("Predicted Outliers:" + predicted_outliers.size());
            System.out.println(StringHelper.toString(predicted_outliers));

            System.out.println("F1 Score: " + F1Score.score(expected_outliers, predicted_outliers, trainingBatch.tupleCount()));
        }
    }


}
