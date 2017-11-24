package com.github.chen0040.ml.spark.text.topicmodeling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.text.Vocabulary;
import com.github.chen0040.ml.spark.utils.views.AlgorithmViewTimePoint;
import com.github.chen0040.ml.spark.utils.views.AlgorithmViewTimeSeries;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 9/16/15.
 * probabilistic Latent Semantic Analysis
 */


public class pLSA extends AbstractSparkMLModule {

    public static final String MAX_ITERS = "MaxIters";
    public static final String TOPIC_COUNT = "TopicCount";
    public static final String MAX_DOC_COUNT_PER_BATCH = "maxDocCountPerBatch";
    public static final String MAX_TOPIC_SUMMARY_LENGTH = "maxTopicSummaryLength";

    private int maxIters(){
        return (int)getAttribute(MAX_ITERS);
    }

    private int topicCount(){
        return (int)getAttribute(TOPIC_COUNT);
    }

    private void _maxTopicSummaryLength(int length){
        setAttribute(MAX_TOPIC_SUMMARY_LENGTH, length);
    }

    private int maxTopicSummaryLength(){
        return (int)getAttribute(MAX_TOPIC_SUMMARY_LENGTH);
    }

    private void _maxDocCountPerBatch(int maxSize){
        setAttribute(MAX_DOC_COUNT_PER_BATCH, maxSize);
    }

    public int maxDocCountPerBatch(){
        return (int)getAttribute(MAX_DOC_COUNT_PER_BATCH);
    }

    private double loglikelihood = Double.NEGATIVE_INFINITY;

    private pLSAModel model;

    public pLSAModel getModel(){
        return model;
    }

    public void setModel(pLSAModel model){
        this.model = model;
    }

    public pLSA(){
        setAttribute(MAX_ITERS, 100);
        setAttribute(TOPIC_COUNT, 10);

        _maxTopicSummaryLength(10);

        _maxDocCountPerBatch(1000000);
    }



    public double getLoglikelihood() {
        return loglikelihood;
    }

    public void setLoglikelihood(double loglikelihood) {
        this.loglikelihood = loglikelihood;
    }

    @Override
    public Object clone() {
        pLSA clone = new pLSA();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        pLSA rhs2 = (pLSA)rhs;
        this.model = (pLSAModel)rhs2.model.clone();
        this.loglikelihood = rhs2.loglikelihood;
    }





    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        JavaRDD<SparkMLTuple> batch = convertToBatch(tuple, sc);

        pLSABatchUpdateResult result = (pLSABatchUpdateResult)batchUpdate(batch);

        int topicCount = result.topicCount();
        int docCount = result.docCount();

        double[][] probability_doc_given_topic = result.probability_doc_given_topic;
        List<AlgorithmViewTimeSeries> docTimeSeriesList = new ArrayList<>();
        for(int topicIndex = 0; topicIndex < topicCount; ++topicIndex){
            AlgorithmViewTimeSeries docTimeSeries = new AlgorithmViewTimeSeries();
            docTimeSeries.setName(result.topicSummary(topicIndex));
            double[] probability_doc = probability_doc_given_topic[topicIndex];
            for(int docIndex = 0; docIndex < docCount; ++docIndex){
                docTimeSeries.getData().add(new AlgorithmViewTimePoint(topicIndex, probability_doc[docIndex]));
            }
        }

        double[][] correlation = result.getTopicCorrelations();

        PredictionResult predictedValue = new PredictionResult();

        predictedValue.addFeature("topicCount", ""+topicCount);
        predictedValue.addFeature("docCount", "" + docCount);
        predictedValue.addFeature("timeseries", JSON.toJSONString(docTimeSeriesList, SerializerFeature.BrowserCompatible));
        predictedValue.addFeature("correlations", JSON.toJSONString(correlation, SerializerFeature.BrowserCompatible));

        return predictedValue;
    }

    private JavaRDD<SparkMLTuple> convertToBatch(SparkMLTuple tuple, JavaSparkContext sc){
        List<String> children = tuple.getChildren();

        List<SparkMLTuple> childList = new ArrayList<>();
        for(int i=0; i < children.size(); ++i){
            String childJson = children.get(i);
            SparkMLTuple child = JSON.parseObject(childJson, SparkMLTuple.class);
            childList.add(child);
        }

        JavaRDD<SparkMLTuple> childRdd = sc.parallelize(childList);



        return childRdd;
    }



    private void initializeModel(JavaRDD<SparkMLTuple> batch){
        JavaRDD<String> batch2 = batch.flatMap(sparkMLTuple -> {
            Doc2Words d2w = new Doc2Words();
            List<String> result = d2w.wordsFromDoc(sparkMLTuple);
            return result;
        });

        JavaRDD<String> batch3 = batch2.distinct();

        List<String> words = batch3.collect();

        model = new pLSAModel();
        model.topicCount = topicCount();
        model.maxTopicSummaryLength = maxTopicSummaryLength();
        model.initialize(words, (int) batch.count());


    }

    /*
    public List<Map.Entry<Integer, Double>> topRankingTopics4Doc(int doc, int limits){
        return model.topRankingTopics4Doc(doc, limits);
    }

    public List<Map.Entry<Integer, Double>> topRankingDocs4Topic(int topic, int limits){
        return model.topRankingDocs4Topic(topic, limits);
    }
    */

    public List<Map.Entry<Integer, Double>> topRankingWords4Topic(int topic, int limits){
        return model.topRankingWords4Topic(topic, limits);
    }

    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch){
        pLSABatchUpdateResult result = new pLSABatchUpdateResult();
        initializeModel(batch);



        initializeModel(batch);

        int docCount = (int)batch.count();

        if(docCount > maxDocCountPerBatch()){
            double fraction = (double)maxDocCountPerBatch() / docCount;
            batch = batch.sample(false, fraction);
            docCount = (int)Math.ceil(fraction * docCount);
        }

        JavaSparkContext sc = new JavaSparkContext(batch.context());

        JavaPairRDD<SparkMLTuple, Long> indexed_rdd = batch.zipWithIndex();

        JavaPairRDD<Integer, HashMap<String, Integer>> word_count_rdd = indexed_rdd.mapToPair(tt -> {

            SparkMLTuple sparkMLTuple = tt._1();
            int index = tt._2().intValue();

            HashMap<String, Integer> wordCount = new HashMap<>();


            Doc2Words d2w = new Doc2Words();
            List<String> words = d2w.wordsFromDoc(sparkMLTuple);
            for (int i = 0; i < words.size(); ++i) {
                String word = words.get(i);
                if (wordCount.containsKey(word)) {
                    wordCount.put(word, wordCount.get(word) + 1);
                } else {
                    wordCount.put(word, 1);
                }
            }
            Tuple2<Integer, HashMap<String, Integer>> tuple2 = new Tuple2<>(index, wordCount);
            return tuple2;
        });


        int topicCount = model.topicCount;
        int wordCount = model.wordCount;

        result.initialize(docCount, model);

        double[][][] probability_topic_given_doc_and_word = new double[docCount][][];
        double[][] probability_doc_given_topic = new double[topicCount][];
        for(int topic = 0; topic < topicCount; ++topic) {
            probability_doc_given_topic[topic] = new double[docCount];

            for(int doc = 0; doc < docCount; ++doc){
                probability_doc_given_topic[topic][doc] = model.random();
            }
            pLSAModel.normalize(probability_doc_given_topic[topic]);
        }

        for(int doc = 0; doc < docCount; ++doc){
            probability_topic_given_doc_and_word[doc] = new double[wordCount][];

            for(int word = 0; word < wordCount; ++word){
                probability_topic_given_doc_and_word[doc][word] = new double[topicCount];
            }
        }


        Broadcast<Vocabulary> vocabularyBroadcast = sc.broadcast(model.vocabulary);
        Broadcast<Integer> wordCountBroadcast = sc.broadcast(wordCount);
        Broadcast<Integer> topicCountBroadcast = sc.broadcast(topicCount);

        for(int iters = 0; iters < maxIters(); ++iters){

            System.out.println("Iters: "+iters);

            // E-step
            for(int doc = 0; doc < docCount; ++doc){
                for(int word = 0; word < wordCount; ++word) {
                    for(int topic = 0; topic < topicCount; ++topic) {
                        probability_topic_given_doc_and_word[doc][word][topic] = model.probability_topic[topic]
                                * probability_doc_given_topic[topic][doc]
                                * model.probability_word_given_topic[topic][word];
                    }

                    pLSAModel.normalize(probability_topic_given_doc_and_word[doc][word]);
                }

            }

            Broadcast<double[][][]> modelBroadcast = sc.broadcast(probability_topic_given_doc_and_word);

            // M-step

            // update P (word | topic) /prop sum_{doc} (P(topic | word, doc) * count(word in doc))
            JavaRDD<double[][]> batch3 = word_count_rdd.map(tuple2 -> {
                int doc = tuple2._1();
                HashMap<String, Integer> wordCounts = tuple2._2();

                double[][][] probability_topic_given_doc_and_word_local = modelBroadcast.getValue();
                int wordCount_local = wordCountBroadcast.value();
                int topicCount_local = topicCountBroadcast.value();
                Vocabulary vocabulary_local = vocabularyBroadcast.value();

                double[][] temp = new double[topicCount_local][];
                for(int topic = 0; topic < topicCount_local; ++topic) {
                    temp[topic] = new double[wordCount_local];
                    for (int word = 0; word < wordCount_local; ++word) {
                        temp[topic][word] = probability_topic_given_doc_and_word_local[doc][word][topic] * wordCounts.getOrDefault(vocabulary_local.get(word), 0);
                    }
                }
                return temp;
            });

            double[][] sum = batch3.reduce((val1, val2) -> {
                int wordCount_local = wordCountBroadcast.value();
                int topicCount_local = topicCountBroadcast.value();

                double[][] temp = new double[topicCount_local][];
                for(int topic = 0; topic < topicCount_local; ++topic) {
                    temp[topic]=new double[wordCount_local];
                    for (int word = 0; word < wordCount_local; ++word) {
                        temp[topic][word] = val1[topic][word] + val2[topic][word];
                    }
                }
                return temp;
            });


            for(int topic = 0; topic < model.topicCount; ++topic) {
                for (int word = 0; word < model.wordCount; ++word) {
                    model.probability_word_given_topic[topic][word] = sum[topic][word];
                }
                pLSAModel.normalize(model.probability_word_given_topic[topic]);
            }



            JavaPairRDD<Integer, double[]> batch33 = word_count_rdd.mapToPair((tuple2) -> {
                int doc = tuple2._1();
                HashMap<String, Integer> wordCounts = tuple2._2();

                int wordCount_local = wordCountBroadcast.value();
                int topicCount_local = topicCountBroadcast.value();

                double[][][] probability_topic_given_doc_and_word_local = modelBroadcast.value();
                Vocabulary vocabulary_local = vocabularyBroadcast.value();

                double[] temp = new double[topicCount_local];
                for (int topic = 0; topic < topicCount_local; ++topic) {
                    // update P (doc | topic) /prop sum_{word} (P(topic | word, doc) * count(word in doc))
                    double sum_local = 0;
                    for (int word = 0; word < wordCount_local; ++word) {
                        sum_local += probability_topic_given_doc_and_word_local[doc][word][topic] * wordCounts.getOrDefault(vocabulary_local.get(word), 0);
                    }
                    temp[topic] = sum_local;
                }

                return new Tuple2<>(doc, temp);
            });

            List<Tuple2<Integer, double[]>> batch3list = batch33.collect();

            for(int j=0; j < batch3list.size(); ++j){
                Tuple2<Integer, double[]> tuple2 = batch3list.get(j);
                int doc = tuple2._1();
                double[] temp = tuple2._2();
                for(int topic = 0; topic < topicCount; ++topic) {
                    probability_doc_given_topic[topic][doc] = temp[topic];
                }
            }

            for(int topic = 0; topic < model.topicCount; ++topic) {
                pLSAModel.normalize(probability_doc_given_topic[topic]);
            }

            JavaRDD<double[]> batch4 = word_count_rdd.map(tuple2 -> {
                int doc = tuple2._1();
                HashMap<String, Integer> wordCounts = tuple2._2();

                int wordCount_local = wordCountBroadcast.value();
                int topicCount_local = topicCountBroadcast.value();

                Vocabulary vocabulary_local = vocabularyBroadcast.value();
                double[][][] probability_topic_given_doc_and_word_local = modelBroadcast.value();

                double[] temp = new double[topicCount_local];
                for(int topic = 0; topic < topicCount_local; ++topic) {
                    double sum_local = 0;
                    for (int word = 0; word < wordCount_local; ++word) {
                        sum_local += probability_topic_given_doc_and_word_local[doc][word][topic] * wordCounts.getOrDefault(vocabulary_local.get(word), 0);
                    }
                    temp[topic] = sum_local;
                }
                return temp;
            });

            double[] sum2 = batch4.reduce((val1, val2) -> {
                int topicCount_local = topicCountBroadcast.value();
                double[] temp = new double[topicCount_local];
                for(int topic=0; topic < topicCount_local; ++topic) {
                    temp[topic] = val1[topic] + val2[topic];
                }
                return temp;
            });

            for(int topic=0; topic < model.topicCount; ++topic) {
                model.probability_topic[topic] = sum2[topic];
            }
            // Normalize
            pLSAModel.normalize(model.probability_topic);

            loglikelihood = calcLogLikelihood(word_count_rdd, probability_doc_given_topic, wordCountBroadcast, topicCountBroadcast, vocabularyBroadcast);

            modelBroadcast.unpersist(true);
        }


        return result;
    }

    private double calcLogLikelihood(JavaPairRDD<Integer, HashMap<String, Integer>> batch,
                                     double[][] probability_doc_given_topic,
                                     Broadcast<Integer> wordCountBroadcast,
                                     Broadcast<Integer> topicCountBroadcast,
                                     Broadcast<Vocabulary> vocabularyBroadcast){

        return batch.map(tuple2 -> {
            int doc = tuple2._1();
            HashMap<String, Integer> wordCounts = tuple2._2();

            int N = wordCountBroadcast.value();
            int topicCount = topicCountBroadcast.value();
            Vocabulary vocabulary = vocabularyBroadcast.value();

            int L = 0;
            for(int word = 0; word < N; ++word) {
                double[] values = new double[topicCount];
                double sum = 0;

                for(int topic = 0; topic < topicCount; ++topic) {
                    double value = model.probability_topic[topic]
                            * probability_doc_given_topic[topic][doc]
                            * model.probability_word_given_topic[topic][word];

                    values[topic] = value;
                    sum += value;
                }

                L += wordCounts.getOrDefault(vocabulary.get(word), 0) * Math.log(sum);
            }

            return L;
        }).reduce((val1, val2)->val1+val2);

    }



}

