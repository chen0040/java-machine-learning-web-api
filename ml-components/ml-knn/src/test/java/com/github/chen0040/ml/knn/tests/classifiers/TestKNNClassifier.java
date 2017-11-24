package com.github.chen0040.ml.knn.tests.classifiers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.knn.tests.utils.FileUtils;
import com.github.chen0040.ml.knn.classifiers.KNNClassifier;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by memeanalytics on 20/8/15.
 */
public class TestKNNClassifier {
    @Test
    public void testKNN(){
        IntelliContext batch = new IntelliContext(FileUtils.getResourceFile("data1X.csv"), ",", false);
        List<String> batch_labels = CSVService.getInstance().readStrings(FileUtils.getResourceFile("data1y.csv"), ",", false);

        for(int i = 0; i < batch.tupleCount(); ++i){
            batch.tupleAtIndex(i).setLabelOutput(batch_labels.get(i));
        }

        KNNClassifier classifier = new KNNClassifier();
        classifier.batchUpdate(batch);

        for(int i = 0; i< batch.tupleCount(); ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String predicted_label = classifier.predict(tuple);
            String expected_label = tuple.getLabelOutput();
            System.out.println("predicted: "+predicted_label + "\texpected: "+expected_label);
        }
    }
}
