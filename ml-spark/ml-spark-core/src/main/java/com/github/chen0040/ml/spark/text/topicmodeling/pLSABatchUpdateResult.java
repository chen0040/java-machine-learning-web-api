package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;

import java.util.*;

/**
 * Created by root on 11/3/15.
 */
public class pLSABatchUpdateResult extends BatchUpdateResult {


    public double[][][] probability_topic_given_doc_and_word = null;
    public double[][] probability_doc_given_topic = null;
    private pLSAModel model;
    private int docCount;

    public int topicCount() {
        return model.topicCount;
    }

    public int docCount() {
        return docCount;
    }

    public String topicSummary(int topicIndex) {
        return model.topicSummaries[topicIndex];
    }


    public void initialize(int docCount, pLSAModel model) {
        this.model = model;
        int topicCount = this.model.topicCount;
        int wordCount = this.model.wordCount;
        this.docCount = docCount;

        probability_topic_given_doc_and_word = new double[docCount][][];
        probability_doc_given_topic = new double[topicCount][];

        for (int topic = 0; topic < topicCount; ++topic) {
            probability_doc_given_topic[topic] = new double[docCount];

            for (int doc = 0; doc < docCount; ++doc) {
                probability_doc_given_topic[topic][doc] = model.random();
            }

            model.normalize(probability_doc_given_topic[topic]);
        }

        for (int doc = 0; doc < docCount; ++doc) {
            probability_topic_given_doc_and_word[doc] = new double[wordCount][];

            for (int word = 0; word < wordCount; ++word) {
                probability_topic_given_doc_and_word[doc][word] = new double[topicCount];
            }
        }
    }

    public List<Map.Entry<Integer, Double>> topRankingWords4Topic(int topic, int limits) {
        return model.topRankingWords4Topic(topic, limits);
    }


    public List<Map.Entry<Integer, Double>> topRankingTopics4Doc(int doc, int limits) {
        int topicCount = model.topicCount;
        double[] probability_topic = model.probability_topic;

        final double[] probs = new double[topicCount];
        List<Integer> topic_orders = new ArrayList<Integer>();
        for (int topic = 0; topic < topicCount; ++topic) {
            probs[topic] = probability_topic[topic] * probability_doc_given_topic[topic][doc];
            topic_orders.add(topic);
        }

        Collections.sort(topic_orders, (t1, t2) -> Double.compare(probs[t2], probs[t1]));

        List<Map.Entry<Integer, Double>> topRankedTopics = new ArrayList<>();
        limits = Math.min(limits, topicCount);
        for (int i = 0; i < limits; ++i) {
            int topic = topic_orders.get(i);
            topRankedTopics.add(new AbstractMap.SimpleEntry<>(topic, probs[topic]));
        }
        return topRankedTopics;
    }

    public List<Map.Entry<Integer, Double>> topRankingDocs4Topic(int topic, int limits) {

        final double[] probs = new double[docCount];
        List<Integer> doc_orders = new ArrayList<>();
        for (int doc = 0; doc < docCount; ++doc) {
            probs[doc] = probability_doc_given_topic[topic][doc];
            doc_orders.add(doc);
        }

        Collections.sort(doc_orders, (t1, t2) -> Double.compare(probs[t2], probs[t1]));

        List<Map.Entry<Integer, Double>> topRankedDocs = new ArrayList<>();
        limits = Math.min(limits, docCount);
        for (int i = 0; i < limits; ++i) {
            int doc = doc_orders.get(i);
            topRankedDocs.add(new AbstractMap.SimpleEntry<>(doc, probs[doc]));
        }
        return topRankedDocs;
    }

    public double[][] getTopicCorrelations() {
        int topicCount = model.topicCount;

        double[][] correlationMatrix = new double[topicCount][];
        for (int i = 0; i < topicCount; ++i) {
            correlationMatrix[i] = new double[topicCount];
        }

        // Look at all pairs of topics that occur in the document.
        for (int i = 0; i < topicCount - 1; i++) {
            for (int j = i + 1; j < topicCount; j++) {
                for (int docIndex = 0; docIndex < docCount; docIndex++) {
                    correlationMatrix[i][j] = probability_doc_given_topic[i][docIndex] + probability_doc_given_topic[j][docIndex];
                    correlationMatrix[i][j] = correlationMatrix[j][i];
                }
            }
        }

        return correlationMatrix;
    }

}
