package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.IntelliTuple;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class BinaryClassifierUtils {

    public static boolean isInClass(IntelliTuple tuple, String classLabel){
        return classLabel.equals(tuple.getLabelOutput());
    }
}
