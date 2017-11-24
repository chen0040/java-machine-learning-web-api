package com.github.chen0040.ml.spark.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.utils.es.ESHits;
import com.github.chen0040.ml.spark.utils.es.ESQueryResult;
import com.github.chen0040.ml.spark.utils.es.ESTimeWindowKey;
import com.github.chen0040.ml.spark.utils.es.ESTimeWindowDoc;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 9/7/15.
 */
public class ESFactory implements Serializable {

    private static final Logger logger = Logger.getLogger(String.valueOf(ESFactory.class));

    private static class Holder{
        static ESFactory instance = new ESFactory();
    }

    public static ESFactory getDefault(){
        return Holder.instance;
    }

    public JavaRDD<SparkMLTuple> readJsonOnMessage(JavaSparkContext context, String content, int timeWindowSize, List<String> keywords) {
        ESQueryResult result = getQueryResult(content);
        return readJsonOnMessage(context, result, timeWindowSize, keywords);
    }

    public JavaRDD<SparkMLTuple> readJsonOnMessage(JavaSparkContext context, InputStream isInputData, int timeWindowSize, List<String> keywords) {
        ESQueryResult result = getQueryResult(isInputData);
        return readJsonOnMessage(context, result, timeWindowSize, keywords);
    }

    public JavaRDD<SparkMLTuple> readJsonOnMessage(JavaSparkContext context, InputStream isInputData, int timeWindowSize) {
        ESQueryResult result = getQueryResult(isInputData);
        return readJsonOnMessage(context, result, timeWindowSize);
    }

    public JavaRDD<SparkMLTuple> readJsonOnSeverity(JavaSparkContext context, InputStream isInputData, int timeWindowSize, List<String> severitiesToTrack) {
        ESQueryResult result = getQueryResult(isInputData);
        return readJsonOnSeverity(context, result, timeWindowSize, severitiesToTrack);
    }

    public JavaRDD<String> readMessages(JavaSparkContext context, InputStream isInputData){
        ESQueryResult result = getQueryResult(isInputData);
        List<String> messages = result.messages();
        return context.parallelize(messages);
    }

    public JavaRDD<SparkMLTuple> readJsonOnMessage(JavaSparkContext context, ESQueryResult result, int timeWindowSize, final List<String> keywords){
        //logger.info("json result: "+result.hits.hits.size());
        if(timeWindowSize <= 0){
            timeWindowSize = 1000;
        }

        //logger.info("time window: "+timeWindowSize);

        for(int i=0; i < keywords.size(); ++i){
            keywords.set(i, keywords.get(i).trim());
        }

        Map<ESTimeWindowKey, ESTimeWindowDoc> docs = result.flatMapByKeywords(keywords, timeWindowSize);

        List<ESTimeWindowDoc> docList = new ArrayList<ESTimeWindowDoc>();

        docList.addAll(docs.values());


        JavaRDD<ESTimeWindowDoc> rdd = context.parallelize(docList);

        JavaRDD<SparkMLTuple> batch = rdd.map(new Function<ESTimeWindowDoc, SparkMLTuple>() {
            public SparkMLTuple call(ESTimeWindowDoc doc) throws Exception {
                SparkMLTuple tuple = new SparkMLTuple();
                tuple.setDimension(keywords.size());
                for (int i = 0; i < keywords.size(); ++i) {
                    tuple.put(i, doc.get(keywords.get(i)));
                }
                return tuple;
            }
        });



        return batch;
    }

    public JavaRDD<SparkMLTuple> readJsonOnMessage(JavaSparkContext context, ESQueryResult result, int timeWindowSize){
        //logger.info("json result: "+result.hits.hits.size());
        if(timeWindowSize <= 0){
            timeWindowSize = 1000;
        }

        Map<ESTimeWindowKey, ESHits> docs = result.flatMapByTimeWindow(timeWindowSize);

        List<ESHits> docList = new ArrayList<ESHits>();

        docList.addAll(docs.values());


        JavaRDD<ESHits> rdd = context.parallelize(docList);

        JavaRDD<SparkMLTuple> batch = rdd.map(new Function<ESHits, SparkMLTuple>() {
            public SparkMLTuple call(ESHits doc) throws Exception {
                SparkMLTuple tuple = new SparkMLTuple();
                List<String> messages = doc.messages();
                for(int i=0; i < messages.size(); ++i) {
                    tuple.add(messages.get(i));
                }
                tuple.setTimestamp(doc.getDate());
                return tuple;
            }
        });



        return batch;
    }

    public JavaRDD<SparkMLTuple> readJsonOnSeverity(JavaSparkContext context, ESQueryResult result, int timeWindowSize, final List<String> severitiesToTrack){
        //logger.info("json result: "+result.hits.hits.size());
        if(timeWindowSize <= 0){
            timeWindowSize = 1000;
        }

        //logger.info("time window: "+timeWindowSize);

        for(int i=0; i < severitiesToTrack.size(); ++i){
            severitiesToTrack.set(i, severitiesToTrack.get(i).trim());
        }

        Map<ESTimeWindowKey, ESTimeWindowDoc> docs = result.flatMapBySeverityKeywords(severitiesToTrack, timeWindowSize);

        List<ESTimeWindowDoc> docList = new ArrayList<ESTimeWindowDoc>();

        docList.addAll(docs.values());


        JavaRDD<ESTimeWindowDoc> rdd = context.parallelize(docList);

        JavaRDD<SparkMLTuple> batch = rdd.map(new Function<ESTimeWindowDoc, SparkMLTuple>() {
            public SparkMLTuple call(ESTimeWindowDoc doc) throws Exception {
                SparkMLTuple tuple = new SparkMLTuple();
                tuple.setDimension(severitiesToTrack.size());
                for(int i=0; i < severitiesToTrack.size(); ++i){
                    tuple.put(i, doc.get(severitiesToTrack.get(i)));
                }
                tuple.setTimestamp(doc.startTime());
                return tuple;
            }
        });



        return batch;
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
            ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            result = mapper.readValue(content, ESQueryResult.class);
        }catch(Exception ex){
            logger.log(Level.SEVERE, "getQueryResult failed", ex);
        }

        return result;
    }
}
