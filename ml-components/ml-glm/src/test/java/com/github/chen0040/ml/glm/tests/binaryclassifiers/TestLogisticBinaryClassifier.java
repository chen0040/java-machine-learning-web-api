package com.github.chen0040.ml.glm.tests.binaryclassifiers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.classifiers.F1ScoreResult;
import com.github.chen0040.ml.commons.tuples.TupleTransformColumn;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import com.github.chen0040.ml.glm.binaryclassifiers.LogisticBinaryClassifier;
import com.github.chen0040.ml.glm.tests.utils.FileUtils;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;
import org.testng.annotations.Test;

import static org.junit.Assert.*;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class TestLogisticBinaryClassifier {
    @Test
    public void testBinaryClassifier(){
        final TupleAttributeLevelSource levels = new TupleAttributeLevelSource();


        TupleTransformRules options = new TupleTransformRules();

        int column_use = 3;
        int column_livch = 4;
        int column_age = 5;
        int column_urban = 6;

        levels.excludeLevelAtAttribute("0", column_livch);

        options.setOutputColumnLabel(column_use);
        options.addColumn(column_livch);
        options.addColumn(column_age);
        options.addTransformedColumn(column_age, new TupleTransformColumn("age^2", (index, age) -> age * age));
        options.addColumn(column_urban);

        IntelliContext batch = new IntelliContext(FileUtils.getResourceFile("contraception.csv"), ",", levels, options, true);

        //System.out.println(batch);
        //System.out.println(batch.flattenedDetails());

        LogisticBinaryClassifier classifier = new LogisticBinaryClassifier("Y");
        assertTrue(classifier.batchUpdate(batch).success());

        for(int i = 0; i < batch.tupleCount(); ++i){
            System.out.println(String.format("positive: %.2f\tnegative: %.2f\tpredicted: %d\texpected: %d",
                    classifier.getPositiveScore(batch.tupleAtIndex(i)),
                    classifier.getNegativeScore(batch.tupleAtIndex(i)),
                    classifier.isInClass(batch.tupleAtIndex(i), batch) ? 1 : 0,
                    BinaryClassifierUtils.isInClass(batch.tupleAtIndex(i), classifier.getPositiveClassLabel()) ? 1 : 0));
        }

        F1ScoreResult result = F1Score.score(classifier, batch);

        System.out.println("Precision: "+result.getPrecision());
        System.out.println("Recall: "+result.getRecall());
        System.out.println("F1 Score: "+result.getF1Score());


    }
}
