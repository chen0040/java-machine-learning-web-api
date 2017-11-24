package com.github.chen0040.ml.tests.textmining.topicmodeling;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.tests.textmining.utils.FileUtils;
import com.github.chen0040.ml.textmining.commons.es.BasicBatchJSONFactory;
import com.github.chen0040.ml.textmining.topicmodeling.pLSA;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 9/16/15.
 */
public class pLSATest {
    @Test
    public void testJson(){
        IntelliContext batch = readJson();

        pLSA method = new pLSA();
        method.batchUpdate(batch);

        for(int topic = 0; topic < method.getTopicCount(); ++topic){
            List<Map.Entry<Integer, Double>> topRankedDocs = method.getTopRankingDocs4Topic(topic, 3);
            List<Map.Entry<Integer, Double>> topRankedWords = method.getTopRankingWords4Topic(topic, 3);

            System.out.println("Topic "+topic+": ");

            System.out.println("Top Ranked Document:");
            for(Map.Entry<Integer, Double> entry : topRankedDocs){
                int doc = entry.getKey();
                double prob = entry.getValue();
                System.out.print(doc+"(" + prob +"), ");
            }
            System.out.println();

            System.out.println("Top Ranked Words:");
            for(Map.Entry<Integer, Double> entry : topRankedWords){
                int word = entry.getKey();
                double prob = entry.getValue();
                System.out.print(method.wordAtIndex(word)+"(" + prob +"), ");
            }
            System.out.println();
        }

        for(int doc = 0; doc < method.getDocCount(); ++doc){
            List<Map.Entry<Integer, Double>> topRankedTopics = method.getTopRankingTopics4Doc(doc, 3);
            System.out.print("Doc "+doc+": ");
            for(Map.Entry<Integer, Double> entry : topRankedTopics){
                int topic = entry.getKey();
                double prob = entry.getValue();
                System.out.print(topic+"(" + prob +"), ");
            }
            System.out.println();
        }
    }

    public IntelliContext readJson(){

        try {
            IntelliContext batch = BasicBatchJSONFactory.getDefault().getBatch(new FileInputStream(FileUtils.getResourceFile("json_v0.txt")));

            return batch;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
