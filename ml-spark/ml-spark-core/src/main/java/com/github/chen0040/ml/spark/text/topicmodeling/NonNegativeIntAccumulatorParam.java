package com.github.chen0040.ml.spark.text.topicmodeling;

import org.apache.spark.AccumulatorParam;

/**
 * Created by memeanalytics on 3/11/15.
 */
public class NonNegativeIntAccumulatorParam implements AccumulatorParam<Integer> {
    @Override
    public Integer addAccumulator(Integer i1, Integer i2) {
        Integer val = i1 + i2;
        if (val < 0) {
            val = 0;
        }
        return val;
    }

    @Override
    public Integer addInPlace(Integer i1, Integer i2) {
        i1 += i2;
        if (i1 < 0) {
            i1 = 0;
        }
        return i1;
    }

    @Override
    public Integer zero(Integer integer) {
        return 0;
    }
}
