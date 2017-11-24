package com.github.chen0040.ml.spark.core.arm;

import com.github.chen0040.ml.spark.core.SparkMLModule;

import java.util.List;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public interface AssocRuleMiner extends SparkMLModule {
    List<String> getUniqueItems();
    ItemSetWarehouse getFrequentItemSets();
}
