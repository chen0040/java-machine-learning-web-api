package com.github.chen0040.ml.spark.core;

import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;

public class SparkMLTupleAscendComparator implements Comparator<Tuple2<SparkMLTuple, Double>>, Serializable {

    public static SparkMLTupleAscendComparator INSTANCE = new SparkMLTupleAscendComparator();

    // ascending order
    public int compare(Tuple2<SparkMLTuple, Double> o1, Tuple2<SparkMLTuple, Double> o2) {
        return Double.compare(o1._2, o2._2);
    }

    public boolean equals(Object obj) {
        return false;
    }
}