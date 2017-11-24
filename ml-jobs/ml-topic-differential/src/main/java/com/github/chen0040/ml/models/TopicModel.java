package com.github.chen0040.ml.models;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.*;

/**
 * Created by root on 11/18/15.
 */

@Entity
@IndexCollection(columns = { @Index(name = "rateOfChangeInTopic")})
public class TopicModel {
    @EmbeddedId
    private TopicModelCompoundKey modelKey;



    @Column

    private String companyId;


    @ElementCollection
    @Column
    private List<String> topics ;

    @ElementCollection
    @Column
    private Map<String, Double> wordCounts ;

    @ElementCollection
    @Column
    private Map<String, Double> wordTopicSpecificities;

    @Column
    private double rateOfChangeInTopic;

    @Column
    private double rateOfChangeInTopWords;

    public double getRateOfChangeInTopWords() {
        return rateOfChangeInTopWords;
    }

    public void setRateOfChangeInTopWords(double rateOfChangeInTopWords) {
        this.rateOfChangeInTopWords = rateOfChangeInTopWords;
    }

    public double getRateOfChangeInTopic() {
        return rateOfChangeInTopic;
    }

    public void setRateOfChangeInTopic(double rateOfChange) {
        this.rateOfChangeInTopic = rateOfChange;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public Map<String, Double> getWordCounts() {
        return wordCounts;
    }

    public void setWordCounts(Map<String, Double> wordCounts) {
        this.wordCounts = wordCounts;
    }

    public Map<String, Double> getWordTopicSpecificities() {
        return wordTopicSpecificities;
    }

    public void setWordTopicSpecificities(Map<String, Double> wordTopicSpecificities) {
        this.wordTopicSpecificities = wordTopicSpecificities;
    }

    public TopicModel(){

        topics = new ArrayList<>();
        wordCounts = new HashMap<>();
        wordTopicSpecificities = new HashMap<>();
        modelKey = new TopicModelCompoundKey();
    }

    public TopicModelCompoundKey getModelKey() {
        return modelKey;
    }

    public void setModelKey(TopicModelCompoundKey modelKey) {
        this.modelKey = modelKey;
    }


    @JsonIgnore
    /** PRIMARY KEY(id) **/
    public TopicModelCompoundKey getPartitionKey() {
        return this.getModelKey();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void computeRateOfChangeWithPrevInstance(TopicModel prevModel){

        rateOfChangeInTopWords = computeRateOfChangeInTopWords(prevModel);
        rateOfChangeInTopic = computeRateOfChangeInTopics(prevModel);
    }

    private double computeRateOfChangeInTopics(TopicModel prevModel){
        List<Map<String, Double>> cluster1 = toWordCountCluster(this);
        List<Map<String, Double>> cluster2 = toWordCountCluster(prevModel);

        if(cluster1.isEmpty() || cluster2.isEmpty()) return 0;

        double[] distances1 = new double[cluster1.size()];
        double[] distances2 = new double[cluster2.size()];

        for(int i=0; i < cluster2.size(); ++i){
            distances2[i] = Double.MAX_VALUE;
        }

        for(int i=0; i < cluster1.size(); ++i){
            Map<String, Double> point1 = cluster1.get(i);
            double minDistance = Double.MAX_VALUE;
            for(int j=0; j < cluster2.size(); ++j){
                Map<String, Double> point2 = cluster2.get(j);

                double distance = computeDistance(point1, point2);
                minDistance = Math.min(minDistance, distance);

                distances2[j] = Math.min(distances2[j], distance);
            }
            distances1[i] = minDistance;
        }

        double sum = 0;
        for(int i=0; i < distances1.length; ++i){
            sum += distances1[i];
        }
        for(int i=0; i < distances2.length; ++i){
            sum += distances2[i];
        }

        sum /= (distances1.length + distances2.length);

        return sum;
    }

    private double computeRateOfChangeInTopWords(TopicModel prevModel){
        return computeDistance(wordCounts, prevModel.wordCounts);
    }



    private static List<Map<String, Double>> toWordCountCluster(TopicModel model){
        List<String> topics = model.getTopics();
        List<Map<String, Double>> result = new ArrayList<>();
        for(int i=0; i < topics.size(); ++i){
            String topic = topics.get(i);
            Map<String, Double> wordCounts = toWordCounts(topic);
            result.add(wordCounts);
        }
        return result;
    }

    private static Map<String, Double> toWordCounts(String doc){
        String[] words = doc.split(" ");
        HashMap<String, Double> wordCounts = new HashMap<>();
        for(int i=0; i < words.length; ++i){
            String word = words[i];
            if(wordCounts.containsKey(word)){
                wordCounts.put(word, wordCounts.get(word)+1);
            } else {
                wordCounts.put(word, 1.0);
            }
        }
        return wordCounts;
    }

    private static double computeDistance(Map<String, Double> doc1, Map<String, Double> doc2){
        HashSet<String> wordUnion = new HashSet<>();
        for(String word : doc1.keySet()){
            wordUnion.add(word);
        }
        for(String word : doc2.keySet()){
            wordUnion.add(word);
        }

        List<String> words = new ArrayList<>();
        for(String word : wordUnion){
            words.add(word);
        }

        double[] wordVec1 = new double[words.size()];
        double[] wordVec2 = new double[words.size()];

        for(int i=0; i < words.size(); ++i){
            String word = words.get(i);
            double count = 0;
            if(doc1.containsKey(word)){
                count = doc1.get(word);
            }
            wordVec1[i] = count;

            count = 0;
            if(doc2.containsKey(word)){
                count = doc2.get(word);
            }
            wordVec2[i] = count;
        }

        double distance = 1 - cosineSimilarity(wordVec1, wordVec2);
        return distance;
    }

    private static double cosineSimilarity(double[] vec1, double[] vec2){
        double len1 = length(vec1);
        double len2 = length(vec2);

        double dp = dotProduct(vec1, vec2);
        return dp / (len1 * len2);
    }

    private static double dotProduct(double[] vec1, double[] vec2){
        double sum = 0;
        for(int i=0; i < vec1.length; ++i){
            sum += (vec1[i] * vec2[i]);
        }
        return sum;
    }

    private static double length(double[] vec){
        double sum = 0;
        for(int i=0; i < vec.length; ++i){
            sum += (vec[i] * vec[i]);
        }
        return Math.sqrt(sum);
    }
}
