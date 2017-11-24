package com.github.chen0040.ml.tests.bayes.nbc;

import com.github.chen0040.ml.bayes.nbc.NBC;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.tests.bayes.utils.FileUtils;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class TestNBC {
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

        NBC svc = new NBC();
        svc.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String predicted_label = svc.predict(tuple);
            System.out.println("predicted: "+predicted_label+"\texpected: "+tuple.getLabelOutput());
        }
    }
}
