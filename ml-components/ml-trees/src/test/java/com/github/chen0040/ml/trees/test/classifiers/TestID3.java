package com.github.chen0040.ml.trees.test.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.trees.test.utils.FileUtils;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.trees.id3.ID3;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class TestID3 {
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

        //System.out.println(batch);

        ID3 svc = new ID3();
        svc.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String predicted_label = svc.predict(tuple);
            System.out.println("predicted: "+predicted_label+"\texpected: "+tuple.getLabelOutput());
        }


    }
}
