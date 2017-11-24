package com.github.chen0040.ml.tests.discrete.kmeans;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.dataprepare.discretize.kmeans.KMeansDiscretizer;
import com.github.chen0040.ml.tests.discrete.utils.FileUtils;
import com.github.chen0040.ml.commons.IntelliContext;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class TestKMeans {
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

        KMeansDiscretizer svc = new KMeansDiscretizer();
        svc.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            IntelliTuple discrete_tuple = svc.discretize(tuple);
            System.out.println(discrete_tuple);
        }
    }
}
