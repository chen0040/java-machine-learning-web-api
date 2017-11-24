package com.github.chen0040.ml.sdk.services;

import com.alibaba.fastjson.JSON;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.readers.DocReaderHelper;
import com.github.chen0040.ml.sdk.models.es.ESQueryResult;
import com.github.chen0040.ml.sdk.models.es.ESTimeWindowDoc;
import com.github.chen0040.ml.sdk.models.es.ESTimeWindowKey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 9/7/15.
 */
public class JSONReaderHelper extends DocReaderHelper {

    private static final Logger logger = Logger.getLogger(String.valueOf(JSONReaderHelper.class));

    public static IntelliContext readElasticSearchFormatJson(String content, int timeWindowSize, List<String> keywords) {

        ESQueryResult result = getQueryResult(content);
        return readElasticSearchFormatJson(result, timeWindowSize, keywords);
    }

    public static IntelliContext readElasticSearchFormatJson(InputStream isInputData, int timeWindowSize, List<String> keywords) {
        ESQueryResult result = getQueryResult(isInputData);
        return readElasticSearchFormatJson(result, timeWindowSize, keywords);
    }

    public static IntelliContext readElasticSearchFormatJson(ESQueryResult result, int timeWindowSize, List<String> keywords){
        IntelliContext batch = new IntelliContext();

        //logger.info("json result: "+result.hits.hits.size());
        if(timeWindowSize <= 0){
            timeWindowSize = 1000;
        }

        //logger.info("time window: "+timeWindowSize);

        for(int i=0; i < keywords.size(); ++i){
            keywords.set(i, keywords.get(i).trim());
        }

        Map<ESTimeWindowKey, ESTimeWindowDoc> docs = result.flatMapByKeywords(keywords, timeWindowSize);

        //logger.info("map result: "+docs.size());

        for(int j=0; j < keywords.size(); ++j){
            batch.getAttributeLevelSource().setAttributeName(j, keywords.get(j));
        }

        for(ESTimeWindowKey key : docs.keySet()) {
            IntelliTuple tuple = batch.newTuple();

            ESTimeWindowDoc doc = docs.get(key);

            HashMap<String, Integer> counts = new HashMap<String, Integer>();
            int m = keywords.size();
            for (int j = 0; j < m; j++) {
                String keyword = keywords.get(j);

                int count = doc.containsKey(keyword) ? doc.get(keyword).intValue() : 0;
                counts.put(keyword, count);
                tuple.set(j, (double)count);
            }

            //tuple.setLabelOutput(doc.label);

            batch.add(tuple);

            BasicDocument doc2 = new BasicDocument();
            doc2.setRawContents(doc.rawContents());
            doc2.setWordCounts(counts);
            doc2.setAttribute("Start Time", getDateString(key.getStartTime()));
            doc2.setAttribute("End Time", getDateString(key.getEndTime()));
            doc2.setTimestamp(key.getStartTime());

            batch.addDoc(doc2);
        }

        return batch;
    }

    private static Date getDate(long timestamp){
        Date date = new Date(timestamp);
        return date;
    }

    private static String getDateString(long timestamp){
        return getDateString(getDate(timestamp));
    }

    private static String getDateString(Date date){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(date);
    }

    public static ESQueryResult getQueryResult(InputStream isInputData){
        ESQueryResult result = null;
        try {
            BufferedReader fp = new BufferedReader(new InputStreamReader(isInputData));

            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = fp.readLine();
                if (line == null) break;
                sb.append(line);
            }

            result = getQueryResult(sb.toString());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "getQueryResult failed", ex);
        }

        return result;
    }

    public static ESQueryResult getQueryResult(String content){
        ESQueryResult result = JSON.parseObject(content, ESQueryResult.class);

        return result;
    }
}
