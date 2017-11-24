package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.IntelliContext;

/**
 * Created by memeanalytics on 13/8/15.
 */
public class PredictionScore {

    public static double score(BinaryClassifier classifier, IntelliContext Xval){
        return score(classifier, Xval, false);
    }

    public static double score(BinaryClassifier classifier, IntelliContext Xval, boolean verbal)
    {
        int sample_count = Xval.tupleCount();

        double correct_count = 0;
        int total_count = 0;
        for (int i = 0; i < sample_count; ++i)
        {
            IntelliTuple rec = Xval.tupleAtIndex(i);

            boolean predicted_positive = classifier.isInClass(rec, Xval);
            boolean is_positive = BinaryClassifierUtils.isInClass(rec, classifier.getPositiveClassLabel());

            System.out.println("Predicted: "+predicted_positive+"\tExpected: "+is_positive);

            if (predicted_positive == is_positive)
            {
                correct_count += 1;
            }
            total_count++;
        }

        return correct_count / total_count;
    }


}
