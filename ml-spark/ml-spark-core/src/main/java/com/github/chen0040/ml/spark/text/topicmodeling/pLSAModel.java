package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.text.Vocabulary;
import com.github.chen0040.ml.spark.text.BasicVocabulary;

import java.io.Serializable;
import java.util.*;

/**
 * Created by root on 11/2/15.
 */
public class pLSAModel implements Cloneable, Serializable {

    public double[] probability_topic = null;


    public double[][] probability_word_given_topic = null;
    public int topicCount = 20;
    public int docCount = -1;
    public int wordCount = -1;
    public Vocabulary vocabulary;
    private Random random = new Random();
    public String[] topicSummaries = null;
    public int maxTopicSummaryLength = 4;

    public double random(){
        return random.nextDouble();
    }

    public pLSAModel(){
        vocabulary = new BasicVocabulary();
    }

    public void createTopicSummary(){
        for(int topicIndex = 0; topicIndex < topicCount; ++topicIndex){
            String topicSummary = getTopicSummary(topicIndex, maxTopicSummaryLength);
            topicSummaries[topicIndex] = topicSummary;
        }
    }

    public void initialize(List<String> words, int docCount){
        vocabulary = new BasicVocabulary();
        vocabulary.setWords(words);

        this.docCount = docCount;

        wordCount = vocabulary.getLength();

        probability_topic = new double[topicCount];
        probability_word_given_topic = new double[topicCount][];

        topicSummaries = new String[topicCount];

        for(int topic = 0; topic < topicCount; ++topic){
            topicSummaries[topic] = "Topic #"+topic;
        }

        for(int topic = 0; topic < topicCount; ++topic) {
            probability_topic[topic] = 1.0 / topicCount;

            probability_word_given_topic[topic] = new double[wordCount];

            for(int word = 0; word < wordCount; ++word){
                probability_word_given_topic[topic][word] = random.nextDouble();
            }
            normalize(probability_word_given_topic[topic]);
        }
    }

    public static void normalize(double[] values){
        int m = values.length;
        double sum = sum(values);
        if(sum > 0) {
            for (int i = 0; i < m; ++i) {
                values[i] /= sum;
            }
        }
    }

    public static double sum(double[] values){
        double sum = 0;
        for(int i=0; i < values.length; ++i){
            sum += values[i];
        }
        return sum;
    }

    @Override
    public Object clone(){
        pLSAModel clone = new pLSAModel();
        clone.copy(this);

        return clone;
    }

    public void copy(pLSAModel rhs2){
        this.probability_topic = clone(rhs2.probability_topic);
        this.probability_word_given_topic = clone(rhs2.probability_word_given_topic);
        this.topicCount = rhs2.topicCount;
        this.docCount = rhs2.docCount;
        this.wordCount = rhs2.wordCount;
        this.vocabulary = (Vocabulary) ((BasicVocabulary)rhs2.vocabulary).clone();
        this.topicSummaries = rhs2.topicSummaries.clone();
        this.maxTopicSummaryLength = rhs2.maxTopicSummaryLength;
        this.topicSummaries = rhs2.topicSummaries.clone();
    }

    private double[][][] clone(double[][][] rhs){
        if(rhs==null) return null;
        int m = rhs.length;
        double[][][] clone = new double[m][][];
        for(int i=0; i < m; ++i){
            clone[i] = clone(rhs[i]);
        }
        return clone;
    }

    private double[][] clone(double[][] rhs){
        if(rhs==null) return null;
        int m = rhs.length;
        double[][] clone = new double[m][];
        for(int i=0; i < m; ++i){
            clone[i] = clone(rhs[i]);
        }
        return clone;
    }

    private double[] clone(double[] rhs){
        if(rhs == null) return null;
        int m = rhs.length;
        double[] clone = new double[m];
        for(int i=0; i < m; ++i){
            clone[i] = rhs[i];
        }
        return clone;
    }

    public String wordAtIndex(int word){
        return vocabulary.get(word);
    }

    public String getTopicSummary(int topic, int length){
        List<Map.Entry<Integer, Double>> rankedWords = topRankingWords4Topic(topic, length);

        StringBuilder sb = new StringBuilder();
        for(int i=0; i < rankedWords.size(); ++i){
            Map.Entry<Integer, Double> item = rankedWords.get(i);
            int wordIndex = item.getKey();
            String word = vocabulary.get(wordIndex);
            if(i != 0){
                sb.append(" ");
            }
            sb.append(word);
        }

        return sb.toString();
    }

    public List<Map.Entry<Integer, Double>> topRankingWords4Topic(int topic, int limits){
        final double[] probs = new double[wordCount];
        List<Integer> word_orders = new ArrayList<Integer>();
        for(int word = 0; word < wordCount; ++word){
            probs[word] = probability_word_given_topic[topic][word];
            word_orders.add(word);
        }

        Collections.sort(word_orders, new Comparator<Integer>() {
            public int compare(Integer t1, Integer t2) {
                return Double.compare(probs[t2], probs[t1]);
            }
        });

        List<Map.Entry<Integer, Double>> topRankedWords = new ArrayList<Map.Entry<Integer, Double>>();
        limits = Math.min(limits, wordCount);
        for(int i = 0; i < limits; ++i){
            int wordIndex = word_orders.get(i);
            topRankedWords.add(new AbstractMap.SimpleEntry<Integer, Double>(wordIndex, probs[wordIndex]));
        }
        return topRankedWords;

    }

}
