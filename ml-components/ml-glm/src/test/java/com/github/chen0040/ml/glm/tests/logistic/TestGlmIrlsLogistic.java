package com.github.chen0040.ml.glm.tests.logistic;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.classifiers.BinaryClassifierUtils;
import com.github.chen0040.ml.commons.tuples.TupleTransformColumn;
import com.github.chen0040.ml.glm.logistic.LogisticModel;
import com.github.chen0040.ml.glm.solvers.GlmSolverType;
import com.github.chen0040.ml.glm.tests.utils.FileUtils;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import org.testng.annotations.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.*;

/**
 * Created by memeanalytics on 15/8/15.
 */
public class TestGlmIrlsLogistic {
    @Test
    public void testLogisticRegression(){
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
        options.addTransformedColumn(column_age, new TupleTransformColumn("age^2", new BiFunction<Integer, Double, Double>() {
            public Double apply(Integer index, Double age) {
                return age * age;
            }
        }));
        options.addColumn(column_urban);

        IntelliContext batch = new IntelliContext(FileUtils.getResourceFile("contraception.csv"), ",", levels, options, true);

        LogisticModel classifier = new LogisticModel("Y");
        classifier.setSolverType(GlmSolverType.GlmIrls);
        assertTrue(classifier.batchUpdate(batch).success());

        LogisticModel classifier2 = new LogisticModel("Y");
        classifier2.setSolverType(GlmSolverType.GlmIrlsQr);
        assertTrue(classifier2.batchUpdate(batch).success());

        LogisticModel classifier3 = new LogisticModel("Y");
        classifier3.setSolverType(GlmSolverType.GlmIrlsSvd);
        assertTrue(classifier3.batchUpdate(batch).success());




        for(int i = 0; i < batch.tupleCount(); ++i){
            System.out.println(String.format("predicted(Irls): %.2f\tpredicted(QR): %.2f\tpredicted(SVD): %.2f\texpected: %d",
                    classifier.evaluate(batch.tupleAtIndex(i), batch),
                    classifier2.evaluate(batch.tupleAtIndex(i), batch),
                    classifier3.evaluate(batch.tupleAtIndex(i), batch),
                    BinaryClassifierUtils.isInClass(batch.tupleAtIndex(i), classifier.getClassLabel()) ? 1 : 0));
        }

        //System.out.println(batch);
        //System.out.println(batch.flattenedDetails());

        System.out.println("Coefficients(Irls): "+classifier.getCoefficients());
        System.out.println("Coefficients(QR): "+classifier2.getCoefficients());
        System.out.println("Coefficients(SVD): "+classifier3.getCoefficients());
    }
}
