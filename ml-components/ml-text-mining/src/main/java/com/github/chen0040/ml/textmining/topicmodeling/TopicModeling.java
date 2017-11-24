package com.github.chen0040.ml.textmining.topicmodeling;

import com.github.chen0040.ml.textretrieval.Vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by root on 9/16/15.
 */
public class TopicModeling {

    private Vocabulary vocab;
    private HashMap<String, Double> pw_theta_d = new HashMap<String, Double>(); //
    private HashMap<String, Double> pw_theta_B = new HashMap<String, Double>(); // known
    private HashMap<String, Double> p_z0_w = new HashMap<String, Double>(); // intermediate

    private double p_theta_d = 0.5; // known
    private double p_theta_B = 0.5; // known

    private Random random = new Random();

    public TopicModeling(Vocabulary vocab)
    {
        this.vocab = vocab;
    }

    public void batchUpdate(HashMap<String, Integer> doc){
        List<String> words_in_doc = new ArrayList<>();
        for(String word : doc.keySet()){
            words_in_doc.add(word);
        }

        int m = words_in_doc.size();

        int N = vocab.getLength();

        pw_theta_d.clear();
        p_z0_w.clear();

        double sum = 0;
        for(int i=0; i < N; ++i){
            double value = random.nextDouble();
            String word = vocab.get(i);
            pw_theta_d.put(word, value);
            p_z0_w.put(word, 0.0);
            sum += value;
        }

        for(int i=0; i < N; ++i){
            String word = vocab.get(i);
            pw_theta_d.put(word, pw_theta_d.get(i) / sum);
        }

        while(true){
            // E step
            for(int i=0; i < m; ++i) {
                String word_i = words_in_doc.get(i);
                p_z0_w.put(word_i, p_theta_d * pw_theta_d.getOrDefault(word_i, 0.0) / (p_theta_d * pw_theta_d.getOrDefault(word_i, 0.0) + p_theta_B * pw_theta_B.getOrDefault(word_i, 0.0)));
            }

            // M step
            for(int i=0; i < m; ++i) {
                String w_i = words_in_doc.get(i);

                double sum2 = 0.0;
                for(int j=0; j < N; ++j){
                    String w_j = vocab.get(j);
                    sum2 += doc.getOrDefault(w_j, 0) * p_z0_w.getOrDefault(w_j, 0.0);
                }

                pw_theta_d.put(w_i, doc.get(w_i) * p_z0_w.get(i) / sum2);
            }
        }

    }
}
