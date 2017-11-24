package com.github.chen0040.ml.tests.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.text.tokenizers.BasicTokenizer;
import com.github.chen0040.ml.spark.text.topicmodeling.LdaMllib;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.spark.utils.es.ESQueryResult;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import com.github.chen0040.ml.spark.text.filters.StopWordRemoval;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.linalg.Matrix;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;

/**
 * Created by root on 9/25/15.
 */
public class LdaMllibTest extends MLearnTestCase {

    private static LdaMllib method;
    private static JavaRDD<SparkMLTuple> batch;
    private List<String> vocabulary;

    public LdaMllibTest(){
        super("LDA");
    }

    @Override
    public void setup(){
        super.setup();
        method = new LdaMllib();
    }

    private List<String> getVocabulary(File file){
        HashMap<String, Integer> V = new HashMap<String, Integer>();
        StopWordRemoval stopWordRemoval = new StopWordRemoval();

        vocabulary = new ArrayList<>();
        try{
            ESQueryResult result = ESFactory.getDefault().getQueryResult(new FileInputStream(file));
            List<String> messages = result.messages();
            BasicTokenizer tokenizer = new BasicTokenizer();
            for(String message : messages){
                for(String word : BasicTokenizer.tokenize(message)) {
                    if(containsAlphabets(word) && stopWordRemoval.filter(word)){
                        if(V.containsKey(word)) V.put(word, V.get(word)+1);
                        else V.put(word, 1);
                    }
                }
            }

            SortedMap<Integer, String> limiter = new TreeMap<Integer, String>();
            for(String word : V.keySet()){
                limiter.put(V.get(word), word);
                if(limiter.size() > 200){
                    limiter.remove(limiter.firstKey());
                }
            }


            for(String word : limiter.values()){
                vocabulary.add(word);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return vocabulary;

    }

    private boolean containsAlphabets(String s){
        String pattern= "^[a-zA-Z]*$";
        return s.matches(pattern);
    }

    private JavaRDD<SparkMLTuple> createBatch(){
        File txtFile = FileUtils.getResourceFile("json_v0.txt");
        List<String> keywords = getVocabulary(txtFile);

        int timeWindowSize = 900000;

        JavaRDD<SparkMLTuple> batch = null;
        try {
            InputStream reader = new FileInputStream(txtFile);
            batch = ESFactory.getDefault().readJsonOnMessage(context, reader, timeWindowSize, keywords);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return batch;
    }

    @Test
    public void testLDA(){

        batch = createBatch();

        method.setAttribute(LdaMllib.TOPIC_COUNT, 5);
        method.batchUpdate(batch);


        LDAModel ldaModel = method.getLdaModel();


        // Output topics. Each is a distribution over vocabulary (matching word count vectors)
        System.out.println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize()
                + " vocabulary):");
        Matrix topics = ldaModel.topicsMatrix();

        for (int topic = 0; topic < method.topicCount(); topic++) {

            SortedMap<Double, Integer> topWordsInTopic = new TreeMap<Double, Integer>();

            System.out.print("Topic " + topic + ":");
            for (int word = 0; word < ldaModel.vocabSize(); word++) {
                double p = topics.apply(word, topic);
                topWordsInTopic.put(p, word);
                if(topWordsInTopic.size() > 5){
                    topWordsInTopic.remove(topWordsInTopic.firstKey());
                }
            }

            for(Map.Entry<Double, Integer> entry : topWordsInTopic.entrySet()){
                Integer word = entry.getValue();
                System.out.print(" p("+vocabulary.get(word) +"):" + topics.apply(word, topic));
            }

            System.out.println();
        }

        /*
        File modelFile = new File("myLDAModel");
        if(!modelFile.exists()){
            ldaModel.save(context.sc(), "myLDAModel");
            DistributedLDAModel sameModel = DistributedLDAModel.load(context.sc(), "myLDAModel");
        }*/


    }
}
