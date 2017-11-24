package com.github.chen0040.ml.tests.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.text.topicmodeling.pLSA;
import com.github.chen0040.ml.spark.text.topicmodeling.pLSAModel;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import com.github.chen0040.ml.spark.utils.ESFactory;
import org.apache.spark.api.java.JavaRDD;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by root on 11/2/15.
 */
public class pLSATest extends MLearnTestCase {
    private static pLSA method;
    private static JavaRDD<SparkMLTuple> batch;

    public pLSATest(){
        super("pLSA");
    }

    @Override
    public void setup(){
        super.setup();
        method = new pLSA();
    }


    private boolean containsAlphabets(String s){
        String pattern= "^[a-zA-Z]*$";
        return s.matches(pattern);
    }

    private JavaRDD<SparkMLTuple> createBatch(){
        File txtFile = FileUtils.getResourceFile("json_v0.txt");

        int timeWindowSize = 900000;

        JavaRDD<SparkMLTuple> batch = null;
        try {
            InputStream reader = new FileInputStream(txtFile);
            batch = ESFactory.getDefault().readJsonOnMessage(context, reader, timeWindowSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return batch;
    }

    @Test
    public void testLSA() {

        batch = createBatch();

        method.setAttribute(pLSA.TOPIC_COUNT, 5);
        method.batchUpdate(batch);

        pLSAModel model = method.getModel();

        // Output topics. Each is a distribution over vocabulary (matching word count vectors)
        System.out.println("Learned topics (as distributions over vocab of " + model.wordCount
                + " vocabulary):");


        for (int topic = 0; topic < model.topicCount; topic++) {

            SortedMap<Double, Integer> topWordsInTopic = new TreeMap<Double, Integer>();

            System.out.print("Topic " + topic + ":");
            for (int word = 0; word < model.wordCount; word++) {
                double p = model.probability_word_given_topic[topic][word];
                topWordsInTopic.put(p, word);
                if (topWordsInTopic.size() > 5) {
                    topWordsInTopic.remove(topWordsInTopic.firstKey());
                }
            }

            for (Map.Entry<Double, Integer> entry : topWordsInTopic.entrySet()) {
                Integer word = entry.getValue();
                double p = entry.getKey();

                System.out.print(" p(" + model.wordAtIndex(word) + "):" + p);
            }

            System.out.println();
        }
    }
}
