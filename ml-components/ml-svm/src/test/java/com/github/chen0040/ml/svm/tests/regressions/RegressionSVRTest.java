package com.github.chen0040.ml.svm.tests.regressions;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.svm.regression.SVR;
import com.github.chen0040.ml.svm.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class RegressionSVRTest {

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

        SVR svr = new SVR();
        svr.setQuiet(false);
        svr.batchUpdate(batch);
    }
}
