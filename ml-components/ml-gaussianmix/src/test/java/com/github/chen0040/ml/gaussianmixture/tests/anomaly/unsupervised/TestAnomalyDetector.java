package com.github.chen0040.ml.gaussianmixture.tests.anomaly.unsupervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.gaussianmixture.MultiVariateNormalOutliers;
import com.github.chen0040.sk.utils.StringHelper;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.ml.gaussianmixture.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class TestAnomalyDetector {

    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    @Test
    public void testHeartScale() throws FileNotFoundException {
        File file = FileUtils.getResourceFile("heart_scale");

        IntelliContext batch = CSVReaderHelper.readHeartScaleFormatCsv(new FileInputStream(file));

        MultiVariateNormalOutliers algorithm = new MultiVariateNormalOutliers();
        algorithm.setAttribute(MultiVariateNormalOutliers.AUTO_THRESHOLDING, 0);

        algorithm.batchUpdate(batch);
        algorithm.setThreshold(0);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String predicted_label = algorithm.isAnomaly(tuple) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
            System.out.println("predicted: "+predicted_label);
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

            MultiVariateNormalOutliers algorithm = new MultiVariateNormalOutliers();
            algorithm.batchUpdate(trainingBatch);
            algorithm.setThreshold(threshold);

            List<Integer> predicted_outliers = algorithm.findOutlierPositions(trainingBatch);

            final List<Integer> expected_outliers = CSVService.getInstance().readIntegers(FileUtils.getResourceFile(filename_outliers), " ", false);
            for(int i=0; i < expected_outliers.size(); ++i){
                expected_outliers.set(i, expected_outliers.get(i)-1);
            }

            System.out.println("Expected Outliers:"+expected_outliers.size());
            System.out.println(StringHelper.toString(expected_outliers));

            System.out.println("Predicted Outliers:"+predicted_outliers.size());
            System.out.println(StringHelper.toString(predicted_outliers));

            System.out.println("F1 Score: "+ F1Score.score(expected_outliers, predicted_outliers, trainingBatch.tupleCount()));
        }
    }
}
