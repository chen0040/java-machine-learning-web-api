package com.github.chen0040.ml.svm.tests.anomaly.unsupervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.svm.anomaly.unsupervised.OneClassSVMAnomalyDetector;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import com.github.chen0040.sk.dom.basic.DomElement;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.sk.utils.StringHelper;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;


/**
 * Created by memeanalytics on 14/8/15.
 */
public class AnomalyOneClassSVMTest {
    @Test
    public void testFindOutliers(){
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_outliers = { "outliers1.txt", "outliers2.txt"};

        for(int k=0; k < filenames_X.length; ++k){

            String filename_X = filenames_X[k];
            String filename_outliers = filenames_outliers[k];

            IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            OneClassSVMAnomalyDetector algorithm = new OneClassSVMAnomalyDetector();

            algorithm.batchUpdate(trainingBatch);
            //algorithm.set_nu(); //need to fine sweep the value of nu to tupleAtIndex good result

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
        IntelliContext trainingBatch = new IntelliContext(table);

        table = new DataTable("c1", "c2");
        // add some validation data
        for(int i=0; i < 20; ++i){
            table.addRow(AnomalyClassLabels.IS_NOT_ANOMALY, randn() * 0.3 + 2, randn() * 0.3 + 2);
            table.addRow(AnomalyClassLabels.IS_NOT_ANOMALY, randn() * 0.3 - 2, randn() * 0.3 - 2);
        }
        IntelliContext crossValidationBatch = new IntelliContext(table);

        table = new DataTable("c1", "c2");
        // add some outliers data
        for(int i=0; i < 20; ++i){
            table.addRow(AnomalyClassLabels.IS_ANOMALY, rand(-4, 4), rand(-4, 4));
            table.addRow(AnomalyClassLabels.IS_ANOMALY, rand(-4, 4), rand(-4, 4));
        }
        IntelliContext outliers = new IntelliContext(table);

        OneClassSVMAnomalyDetector algorithm = new OneClassSVMAnomalyDetector();
        algorithm.set_gamma(0.1);
        algorithm.set_nu(0.1);

        algorithm.batchUpdate(trainingBatch);

        for(int i = 0; i < crossValidationBatch.tupleCount(); ++i){
            String predicted = algorithm.isAnomaly(crossValidationBatch.tupleAtIndex(i)) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
            System.out.println("predicted: "+predicted+"\texpected: "+crossValidationBatch.tupleAtIndex(i).getLabelOutput());
        }

        for(int i = 0; i < outliers.tupleCount(); ++i){
            String predicted = algorithm.isAnomaly(outliers.tupleAtIndex(i)) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
            System.out.println("predicted: "+predicted+"\texpected: "+outliers.tupleAtIndex(i).getLabelOutput());
        }


    }

    @Test
    public void testEvaluation(){
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_Xval = {"Xval1.txt", "Xval2.txt"};
        String[] filenames_yval = { "yval1.txt", "yval2.txt"};

        for(int k=0; k < filenames_X.length; ++k){

            String filename_X = filenames_X[k];
            String filename_Xval = filenames_Xval[k];
            String filename_yval = filenames_yval[k];

            IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            OneClassSVMAnomalyDetector algorithm = new OneClassSVMAnomalyDetector();
            algorithm.batchUpdate(trainingBatch);

            IntelliContext crossValidationBatch = new IntelliContext(FileUtils.getResourceFile(filename_Xval), " ", false);

            final List<String> crossValidationLabels = new ArrayList<>();
            CSVService.getInstance().readDoc(FileUtils.getResourceFile(filename_yval), null, false, new Function<DomElement, Boolean>() {
                public Boolean apply(DomElement line) {
                    String[] values = line.data;
                    assertEquals(1, values.length);
                    boolean is_anomaly = StringHelper.parseBoolean(values[0]);
                    if (is_anomaly) crossValidationLabels.add(AnomalyClassLabels.IS_ANOMALY);
                    else crossValidationLabels.add(AnomalyClassLabels.IS_NOT_ANOMALY);
                    return true;
                }
            }, null);

            assertEquals(crossValidationLabels.size(), crossValidationBatch.tupleCount());

            for(int i = 0; i < crossValidationBatch.tupleCount(); ++i){
                crossValidationBatch.tupleAtIndex(i).setLabelOutput(crossValidationLabels.get(i));
            }

            for(int i = 0; i < crossValidationBatch.tupleCount(); ++i){
                String predicted = algorithm.isAnomaly(crossValidationBatch.tupleAtIndex(i)) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
                System.out.println("predicted: "+predicted+"\texpected: "+crossValidationLabels.get(i));
            }
        }
    }
}
