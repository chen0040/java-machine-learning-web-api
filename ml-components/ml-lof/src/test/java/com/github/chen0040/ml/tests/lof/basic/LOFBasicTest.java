package com.github.chen0040.ml.tests.lof.basic;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.lof.LOF;
import com.github.chen0040.ml.tests.lof.utils.FileUtils;
import com.github.chen0040.sk.dom.basic.DomFileInfo;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class LOFBasicTest {




    @Test
    public void testCalcProbability() {
        String[] filenames_X = {"X1.txt", "X2.txt"};
        double[] acceptable_errors = {0.53, 0.001};

        for (int k = 0; k < filenames_X.length; ++k) {
            String filename_X = filenames_X[k];
            double acceptable_error = acceptable_errors[k];

            DomFileInfo fileInfo = CSVService.getInstance().getFileInfo(FileUtils.getResourceFile(filename_X), " ", false, null);

            assertTrue(fileInfo.isTable());

            IntelliContext batch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            LOF algorithm = new LOF();
            algorithm.setAttribute(LOF.AUTO_THRESHOLDING, 0);

            algorithm.batchUpdate(batch);

            for (int i = 0; i < batch.tupleCount(); ++i) {
                double p_predicted = algorithm.evaluate(batch.tupleAtIndex(i), batch);

                System.out.println(String.format("(%d / %d) p = %.3f", i, batch.tupleCount(), p_predicted));
            }
        }

    }



}
