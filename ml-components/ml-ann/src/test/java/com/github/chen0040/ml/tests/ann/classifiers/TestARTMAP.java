package com.github.chen0040.ml.tests.ann.classifiers;

import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.ann.art.classifiers.ARTMAPClassifier;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 23/8/15.
 */
public class TestARTMAP {
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
    public void TestHeartScale() throws FileNotFoundException {
        File file = FileUtils.getResourceFile("heart_scale");

        IntelliContext batch = CSVReaderHelper.readHeartScaleFormatCsv(new FileInputStream(file));


        double best_alpha=0, best_beta=0, best_rho_base=0;
        double predictionAccuracy = 0;
        int nodeCount = batch.tupleCount();

        for(double alpha = 5; alpha < 10; alpha += 0.1) {
            for(double beta = 0; beta < 1; beta += 0.1) {
                for(double rho = 0.01; rho < 1; rho += 0.1){
                    ARTMAPClassifier svc = new ARTMAPClassifier();

                    svc.setAttribute(ARTMAPClassifier.ALPHA, alpha);
                    svc.setAttribute(ARTMAPClassifier.BETA, beta);
                    svc.setAttribute(ARTMAPClassifier.RHO_BASE, rho);

                    svc.batchUpdate(batch);

                    int correctnessCount = 0;
                    for(int i = 0; i < batch.tupleCount(); ++i){
                        IntelliTuple tuple = batch.tupleAtIndex(i);
                        String predicted_label = svc.predict(tuple);
                        //System.out.println("predicted: "+predicted_label+"\texpected: "+tuple.getLabelOutput());
                        correctnessCount += (predicted_label.equals(tuple.getLabelOutput()) ? 1 : 0);
                    }

                    double accuracy = (correctnessCount * 100 / batch.tupleCount());
                    if((accuracy > predictionAccuracy && nodeCount / (double)svc.nodeCount() > 0.8) || (accuracy / predictionAccuracy > 0.85 && nodeCount > svc.nodeCount())){
                        best_alpha = alpha;
                        best_beta = beta;
                        best_rho_base = rho;
                        predictionAccuracy = accuracy;
                        nodeCount = svc.nodeCount();
                    }

                    //System.out.println("Prediction accuracy: " + (correctnessCount * 100 / batch.size()));
                }
            }
        }

        System.out.println("QAlpha: "+best_alpha + "\tbeta: "+best_beta+"\trho_base: "+best_rho_base);
        System.out.println("accuarcy: "+predictionAccuracy);
        System.out.println("nodeCount: "+nodeCount);
        System.out.println("dbSize: "+batch.tupleCount());



    }
}
