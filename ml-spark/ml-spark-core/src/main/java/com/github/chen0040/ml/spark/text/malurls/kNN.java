package com.github.chen0040.ml.spark.text.malurls;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.anomaly.AbstractAnomalyDetecter;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 10/27/15.
 */
public class kNN extends AbstractAnomalyDetecter {

    public static final String K = "K";
    private HashMap<String, String> dataSet = new HashMap<>();


    private void _k(int k){
        setAttribute(K, k);
    }

    public int k(){
        return (int)getAttribute(K);
    }

    public HashMap<String, String> getDataSet(){
        return dataSet;
    }

    public void setDataSet(HashMap<String, String> dataSet){
        this.dataSet = dataSet;
    }

    public kNN(){
        _k(3);
    }

    @Override
    public kNNBatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> trainData) {
        JavaPairRDD<String, String> dataRDD = trainData.mapToPair(tuple -> {
           List<String> words = tuple.toBagOfWords();
            String urlAddress = words.get(0);
            String label = tuple.getLabelOutput();

            return new Tuple2<>(urlAddress, label);
        });

        List<Tuple2<String, String>> dataList = dataRDD.collect();
        dataSet.clear();
        for(int i=0; i < dataList.size(); ++i){
            Tuple2<String, String> item = dataList.get(i);
            dataSet.put(item._1(), item._2());
        }

        return new kNNBatchUpdateResult(dataSet);
    }

    @Override
    public boolean isAnomaly(SparkMLTuple tuple) {
        List<String> words = tuple.toBagOfWords();
        if(words.isEmpty()){
           return false;
        }

        String selectedUrlAddress = words.get(0);
        Map<String, Integer> sortedMap = new HashMap<>();
        for(String urlAddress : dataSet.keySet()){
            int distance = LevenshteinDistance.ComputeDistance(urlAddress, selectedUrlAddress);

            if(sortedMap.size()< k()){
                sortedMap.put(urlAddress, distance);
            } else {
                int maxDistance = 0;
                String worst = null;
                for(String u : sortedMap.keySet()){
                    int distance2 = sortedMap.get(u);
                    if(distance2 > maxDistance){
                        maxDistance = distance2;
                        worst = u;
                    }
                }

                if(maxDistance > distance){
                    sortedMap.remove(worst);
                    sortedMap.put(urlAddress, distance);
                }
            }
        }

        Map<String, Integer> labelCounts = new HashMap<>();
        for(String urlAddress : sortedMap.keySet()){
            String label = dataSet.get(urlAddress);
            if(labelCounts.containsKey(label)){
                labelCounts.put(label, labelCounts.get(label)+1);
            } else {
                labelCounts.put(label, 1);
            }
        }

        String predictedLabel = null;
        int maxCount = 0;
        for(String label : labelCounts.keySet()){
            int labelCount = labelCounts.get(label);
            if(labelCount > maxCount){
                maxCount = labelCount;
                predictedLabel = label;
            }
        }

        return "1".equals(predictedLabel);
    }

    @Override
    public Object clone() {
        kNN clone = new kNN();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        kNN rhs2 = (kNN)rhs;

        dataSet.clear();
        for(Map.Entry<String, String> entry : rhs2.dataSet.entrySet()){
            dataSet.put(entry.getKey(), entry.getValue());
        }
    }

    public HashMap<String, String> getModel() {
        return dataSet;
    }
}
