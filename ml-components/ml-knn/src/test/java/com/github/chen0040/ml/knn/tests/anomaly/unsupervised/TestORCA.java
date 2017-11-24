package com.github.chen0040.ml.knn.tests.anomaly.unsupervised;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.knn.anomaly.ORCA;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class TestORCA {


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


        DataTable table =new DataTable("c1", "c2");
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

        ORCA algorithm = new ORCA();
        algorithm.batchUpdate(batch);
        algorithm.setAnomalyCount(20);
        algorithm.setK(4);
        algorithm.setNumBlocks(10);

        algorithm.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            System.out.println("predicted: " + algorithm.evaluate(tuple, algorithm.getModelSource())+"\tcheck: "+tuple.getPredictedLabelOutput()+"\texpected: "+tuple.getLabelOutput());
        }

        /*
        for(int i=0; i < crossValidationBatch.size(); ++i){
            String predicted = algorithm.isAnomaly(crossValidationBatch.tupleAtIndex(i)) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
            System.out.println("predicted: "+predicted+"\texpected: "+crossValidationBatch.tupleAtIndex(i).getLabelOutput());
        }

        for(int i=0; i < outliers.size(); ++i){
            String predicted = algorithm.isAnomaly(crossValidationBatch.tupleAtIndex(i)) ? AnomalyClassLabels.IS_ANOMALY : AnomalyClassLabels.IS_NOT_ANOMALY;
            System.out.println("predicted: "+predicted+"\texpected: "+outliers.tupleAtIndex(i).getLabelOutput());
        }*/


    }

}
