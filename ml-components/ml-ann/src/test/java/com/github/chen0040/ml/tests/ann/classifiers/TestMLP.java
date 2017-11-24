package com.github.chen0040.ml.tests.ann.classifiers;

import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.ann.mlp.classifiers.MLPClassifier;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by memeanalytics on 19/8/15.
 */
public class TestMLP {
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

        MLPClassifier svc = new MLPClassifier();
        svc.batchUpdate(batch.clone());

        int correctnessCount = 0;
        for(int i = 0; i < batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);

            System.out.println(batch.toString(tuple));



            String predicted_label = svc.predict(tuple);
            System.out.println("predicted: "+predicted_label+"\texpected: "+tuple.getLabelOutput());
            correctnessCount += (predicted_label.equals(tuple.getLabelOutput()) ? 1 : 0);
        }

        System.out.println("Prediction Accurarcy: "+(correctnessCount * 100 / batch.tupleCount()));
    }
}
