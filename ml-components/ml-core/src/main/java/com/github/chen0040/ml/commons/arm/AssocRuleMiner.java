package com.github.chen0040.ml.commons.arm;

import com.github.chen0040.ml.commons.MLModule;

import java.util.List;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public interface AssocRuleMiner extends MLModule{
    List<String> getUniqueItems();
    ItemSetWarehouse getFrequentItemSets();
}
