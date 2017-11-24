package com.github.chen0040.ml.textmining.commons.es;

import com.alibaba.fastjson.JSON;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.commons.IntelliContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 9/7/15.
 */
public class BasicBatchJSONFactory {

    private static final Logger logger = Logger.getLogger(String.valueOf(BasicBatchJSONFactory.class));

    private static class Holder{
        static BasicBatchJSONFactory instance = new BasicBatchJSONFactory();
    }

    public static BasicBatchJSONFactory getDefault(){
        return Holder.instance;
    }

    public IntelliContext getBatch(InputStream isInputData) {
        int timeWindowSize = 900000; // 15 minutes
        IntelliContext batch = new IntelliContext();
        ESQueryResult result = getQueryResult(isInputData);
        fillBatch(batch, result, timeWindowSize);

        return batch;
    }

    public void fillBatch(IntelliContext batch, ESQueryResult result, int timeWindowSize){
        //logger.info("json result: "+result.hits.hits.size());
        if(timeWindowSize <= 0){
            timeWindowSize = 1000;
        }

        Map<ESTimeWindowKey, ESHits> docs = result.flatMapByTimeWindow(timeWindowSize);

        for(ESTimeWindowKey key : docs.keySet()) {
            IntelliTuple tuple = batch.newTuple();

            ESHits doc = docs.get(key);

            HashMap<String, Integer> counts = doc.getWordCounts();


            batch.add(tuple);

            BasicDocument doc2 = new BasicDocument();
            doc2.setRawContents(doc.rawContents());
            doc2.setWordCounts(counts);
            doc2.setAttribute("Start Time", getDateString(key.getStartTime()));
            doc2.setAttribute("End Time", getDateString(key.getEndTime()));
            doc2.setTimestamp(key.getStartTime());

            batch.getDocBag().add(doc2);
        }
    }

    private Date getDate(long timestamp){
        Date date = new Date(timestamp);
        return date;
    }

    private String getDateString(long timestamp){
        return getDateString(getDate(timestamp));
    }

    private String getDateString(Date date){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(date);
    }

    public ESQueryResult getQueryResult(InputStream isInputData){
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

    public ESQueryResult getQueryResult(String content){
        ESQueryResult result = null;
        try {

            result = JSON.parseObject(content, ESQueryResult.class);
        }catch(Exception ex){
            logger.log(Level.SEVERE, "getQueryResult failed", ex);
        }

        return result;
    }
}
