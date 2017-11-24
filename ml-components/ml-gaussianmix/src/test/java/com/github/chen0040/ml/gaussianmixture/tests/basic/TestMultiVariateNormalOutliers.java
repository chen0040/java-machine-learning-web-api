package com.github.chen0040.ml.gaussianmixture.tests.basic;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.gaussianmixture.MultiVariateNormalOutliers;
import com.github.chen0040.sk.dom.basic.DomContent;
import com.github.chen0040.sk.dom.basic.DomFileInfo;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.ml.gaussianmixture.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class TestMultiVariateNormalOutliers {

    @Test
    public void testCalcProbabilityWithCrossValidation() {
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_Xval = {"Xval1.txt", "Xval2.txt"};
        String[] filenames_pval = {"pval1.txt", "pval2.txt"};
        double[] acceptable_errors = {0.56, 0.001};

        for (int k = 0; k < filenames_X.length; ++k) {
            String filename_X = filenames_X[k];
            String filename_pval = filenames_pval[k];
            String filename_Xval = filenames_Xval[k];
            double acceptable_error = acceptable_errors[k];

            IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);
            IntelliContext crossValidationBatch = new IntelliContext(FileUtils.getResourceFile(filename_Xval), " ", false);

            MultiVariateNormalOutliers algorithm = new MultiVariateNormalOutliers();
            algorithm.setAttribute(MultiVariateNormalOutliers.AUTO_THRESHOLDING, 0);

            final List<Double> p_crossValuation = new ArrayList<Double>();
            CSVService.getInstance().readDoc(FileUtils.getResourceFile(filename_pval), null, false, new Consumer<DomContent>() {
                public void accept(DomContent content) {
                    List<List<Double>> lists = content.toTableOfDouble();
                    for (int i = 0; i < lists.size(); ++i) {
                        List<Double> list = lists.get(i);
                        p_crossValuation.add(list.get(0));
                    }
                }
            }, null);

            assertEquals(p_crossValuation.size(), crossValidationBatch.tupleCount());

            algorithm.batchUpdate(trainingBatch);

            double total_error = 0;

            for (int i = 0; i < crossValidationBatch.tupleCount(); ++i) {
                double p_predicted = algorithm.calculateProbability(crossValidationBatch.tupleAtIndex(i));
                double error = Math.abs(p_predicted - p_crossValuation.get(i));
                total_error += error;
                //System.out.println(String.format("p_crossValuation = %.3f correct_p %.3f", p_predicted, p_crossValuation.tupleAtIndex(i)));
            }

            //System.out.println(String.format("Total error: %.2f", total_error));

            assertTrue(total_error < acceptable_error);
        }
    }



    @Test
    public void testCalcProbability() {
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_p = {"p1.txt", "p2.txt"};
        double[] acceptable_errors = {0.53, 0.001};

        for (int k = 0; k < filenames_X.length; ++k) {
            String filename_X = filenames_X[k];
            String filename_p = filenames_p[k];
            double acceptable_error = acceptable_errors[k];

            DomFileInfo fileInfo = CSVService.getInstance().getFileInfo(FileUtils.getResourceFile(filename_X), " ", false, null);

            assertTrue(fileInfo.isTable());

            IntelliContext batch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            MultiVariateNormalOutliers algorithm = new MultiVariateNormalOutliers();
            algorithm.setAttribute(MultiVariateNormalOutliers.AUTO_THRESHOLDING, 0);

            final List<Double> p = CSVService.getInstance().readDoubles(FileUtils.getResourceFile(filename_p), " ", false);

            assertEquals(p.size(), batch.tupleCount());

            algorithm.batchUpdate(batch);

            double total_error = 0;

            for (int i = 0; i < batch.tupleCount(); ++i) {
                double p_predicted = algorithm.calculateProbability(batch.tupleAtIndex(i));
                double error = Math.abs(p_predicted - p.get(i));
                total_error += error;
                //System.out.println(String.format("p = %.3f correct_p %.3f", p_predicted, p.tupleAtIndex(i)));
            }

            System.out.println(String.format("Total error: %.2f", total_error));

            assertTrue(total_error < acceptable_error);
        }

    }



}
