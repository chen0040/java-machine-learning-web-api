package com.github.chen0040.ml.trees.test.anomaly.unsupervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.trees.iforest.IsolationForest;
import com.github.chen0040.ml.trees.test.utils.FileUtils;
import com.github.chen0040.sk.utils.StringHelper;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class IsolationForestTest {
    @Test
    public void testFindOutliers(){
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_outliers = { "outliers1.txt", "outliers2.txt"};
        double[] thresholds = { 0.2943, 0.45};

        for(int k=0; k < filenames_X.length; ++k){

            String filename_X = filenames_X[k];
            String filename_outliers = filenames_outliers[k];
            double threshold = thresholds[k];

            IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            IsolationForest algorithm = new IsolationForest();
            algorithm.batchUpdate(trainingBatch);
            algorithm.setAttribute(IsolationForest.THRESHOLD, threshold);

            List<Integer> predicted_outliers = algorithm.findOutlierPositions(trainingBatch);

            final List<Integer> expected_outliers = CSVService.getInstance().readIntegers(FileUtils.getResourceFile(filename_outliers), " ", false);

            System.out.println("Expected Outliers:"+expected_outliers.size());
            System.out.println(StringHelper.toString(expected_outliers));

            System.out.println("Predicted Outliers:"+predicted_outliers.size());
            System.out.println(StringHelper.toString(predicted_outliers));

            System.out.println("F1 Score: "+ F1Score.score(expected_outliers, predicted_outliers, trainingBatch.tupleCount()));
        }
    }


    private static Random random = new Random();

    public static double rand(){
        return random.nextDouble();
    }

    public static double rand(double lower, double upper){
        return rand() * (upper - lower) + lower;
    }

    public static double randn(){
        double u1 = rand();
        double u2 = rand();
        double r = Math.sqrt(-2.0 * Math.log(u1));
        double theta = 2.0 * Math.PI * u2;
        return r * Math.sin(theta);
    }


    // unit testing based on example from http://scikit-learn.org/stable/auto_examples/svm/plot_oneclass.html#
    @Test
    public void testSimple(){


        DataTable table = new DataTable("c1", "c2");
        // add some normal data
        for(int i=0; i < 100; ++i){
            table.addRow(AnomalyClassLabels.IS_NOT_ANOMALY, randn() * 0.3 + 2, randn() * 0.3 + 2);
            table.addRow(AnomalyClassLabels.IS_NOT_ANOMALY, randn() * 0.3 - 2, randn() * 0.3 - 2);
        }

        // add some outliers data
        for(int i=0; i < 20; ++i){
            table.addRow(AnomalyClassLabels.IS_ANOMALY, rand(-4, 4), rand(-4, 4));
            table.addRow(AnomalyClassLabels.IS_ANOMALY, rand(-4, 4), rand(-4, 4));
        }

        IntelliContext batch = new IntelliContext(table);

        IsolationForest algorithm = new IsolationForest();
        algorithm.setAttribute(IsolationForest.THRESHOLD, 0.38);

        algorithm.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            System.out.println("predicted: " + algorithm.evaluate(tuple, algorithm.getModelSource())+"\tpredicted anomaly: "+algorithm.isAnomaly(tuple)+"\texpected: "+tuple.getLabelOutput());
        }



    }

}
