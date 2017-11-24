package com.github.chen0040.ml.sdk.models.es;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 9/7/15.
 */
public class ESQueryResult {
    public int took;
    public boolean timed_out;
    public ESShards _shards = new ESShards();
    public ESHits hits = new ESHits();


    public void sortAscendinglyByTime(){
        hits.sortAscendinglyByTime();
    }

    // timeWindowSize in milliseconds
    public Map<ESTimeWindowKey, ESHits> flatMapByTimeWindow(int timeWindowSize){
        return hits.flatMapByTimeWindow(timeWindowSize);
    }

    public Map<ESTimeWindowKey, ESTimeWindowDoc> flatMapByKeywords(List<String> keywords, int timeWindowSize){
        Map<ESTimeWindowKey, ESHits> hitsMap = flatMapByTimeWindow(timeWindowSize);
        Map<ESTimeWindowKey, ESTimeWindowDoc> docs = new HashMap<ESTimeWindowKey, ESTimeWindowDoc>();
        for(ESTimeWindowKey key : hitsMap.keySet()){
            ESHits hits2 = hitsMap.get(key);
            ESTimeWindowDoc vec = hits2.reduceByKeywords(keywords);
            docs.put(key, vec);
        }
        return docs;
    }
}
