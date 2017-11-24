package com.github.chen0040.ml.textmining.malurls;

import com.github.chen0040.ml.commons.BatchUpdateResult;

import java.util.HashMap;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class kNNBatchUpdateResult extends BatchUpdateResult {
    private HashMap<String, String> dataSet;

    public kNNBatchUpdateResult(HashMap<String, String> dataSet){
        this.dataSet = dataSet;
    }

    public HashMap<String, String> getDataSet() {
        return dataSet;
    }

    public void setDataSet(HashMap<String, String> dataSet) {
        this.dataSet = dataSet;
    }
}
