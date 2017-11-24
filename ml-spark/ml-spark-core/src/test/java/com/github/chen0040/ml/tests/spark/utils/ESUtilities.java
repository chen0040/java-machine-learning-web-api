package com.github.chen0040.ml.tests.spark.utils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.spark.utils.HttpUtilities;
import com.github.chen0040.ml.spark.utils.UrlData;
import com.github.chen0040.ml.spark.utils.es.ESHit;
import com.github.chen0040.ml.spark.utils.es.ESQueryResult;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chen0469 on 11/6/2015 0006.
 */
public class ESUtilities {
    private static String ipAddress = "d-tomcat-1";

    public static List<Tuple2<String, UrlData>> queryUrls(){
        return queryUrls(200);
    }

    public static List<Tuple2<String, UrlData>> queryUrls(int size){
        List<Tuple2<String, UrlData>> urls;
        int counter = 1;
        do{
            urls = queryUrls(size, 12 * counter);
            counter++;
        }while(urls.isEmpty() && counter < 10);
        return urls;
    }

    public static List<Tuple2<String, UrlData>> queryUrls(int size, int hours){
        String url = "http://"+ipAddress+":9200/syslog/_search?pretty";

        long end = (new Date()).getTime(); //1446778781289L;
        long start = end - hours * 3600 * 1000;

        String data = " { \"size\" : "+size+", \"query\" : { \"bool\" : { \"must\" : [ { \"range\" : { \"Date\" : { \"from\" : "+start+", \"to\" : "+end+", \"include_lower\" : true, \"include_upper\" : false } } }, { \"nested\" : { \"query\" : { \"bool\" : { \"must\" : { \"match\" : { \"tag\" : { \"query\" : \"url\", \"type\" : \"boolean\" } } } } }, \"path\" : \"Tags\" } } ] } } } ";
        String response = HttpUtilities.httpGet(url, data);

        List<Tuple2<String, UrlData>> urlAddresses = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            JsonNode root = mapper.readTree(response);
            JsonNode hits = root.get("hits");

            hits = hits.get("hits");

            int hitCount = hits.size();

            System.out.println("hits: "+hitCount);

            for(int i = 0; i < hitCount; ++i){
                JsonNode hit = hits.get(i);
                JsonNode source = hit.get("_source");
                JsonNode tags = source.get("Tags");
                long timestamp = source.get("Date").longValue();
                String message = source.get("Message").textValue();

                for(int j=0; j < tags.size(); ++j){
                    JsonNode tag = tags.get(j);

                    if(tag==null) continue;
                    JsonNode tagNameNode = tag.get("tag");
                    String tagName = tagNameNode.textValue();

                    if(tagName.equals("url")){
                        String urlAddress = tag.get("data").textValue();
                        UrlData urlData = new UrlData();
                        urlData.setTimestamp(timestamp);
                        urlAddresses.add(new Tuple2<>(message, urlData));
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlAddresses;
    }

    public static ESQueryResult queryES(){
        String json = httpGetES();
        //System.out.println(json);

        ESQueryResult result = ESFactory.getDefault().getQueryResult(json);

        return result;
    }

    public static List<ESHit> queryESHits(){
        ESQueryResult result = queryES();
        return result.hits.hits;
    }

    public static String httpGetES(){
        int recCountES = 10000;
        return httpGetES(recCountES);
    }

    public static String httpGetES(int size){
        String urlES = "http://"+ipAddress+":9200/syslog/syslog/_search";
        String urlAddress = urlES +"?size="+size;
        return HttpUtilities.httpGet(urlAddress);
    }


}
