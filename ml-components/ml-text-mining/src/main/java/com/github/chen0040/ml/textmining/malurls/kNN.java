package com.github.chen0040.ml.textmining.malurls;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.textmining.commons.Tuple2;
import com.github.chen0040.ml.textmining.distance.LevenshteinDistance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by root on 10/27/15.
 */
public class kNN extends AbstractMLModule {

    public static final String K = "K";
    private HashMap<String, String> dataSet = new HashMap<>();
    private Function<String, Boolean> urlParser = null;


    public Function<String, Boolean> getUrlParser() {
        return urlParser;
    }

    public void setUrlParser(Function<String, Boolean> urlParser) {
        this.urlParser = urlParser;
    }

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

    private boolean isMalicious(IntelliTuple tuple){
        String url = tuple.getLabelOutput();
        if(tuple.getTag()!=null && tuple.getTag() instanceof String){
            url = (String)tuple.getTag();
        }

        return isMalicious(url);
    }



    @Override
    public BatchUpdateResult batchUpdate(IntelliContext trainData) {
        List<Tuple2<String, String>> dataList = UrlReader.getLabelledUrls(trainData, urlParser);
        dataSet.clear();

        for(int i=0; i < dataList.size(); ++i){
            Tuple2<String, String> item = dataList.get(i);
            dataSet.put(item._1(), item._2());
        }

        return new kNNBatchUpdateResult(dataSet);
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        return isMalicious(tuple) ? 1 : 0;
    }

    public boolean isMalicious(String selectedUrlAddress) {

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

        if("1".equals(predictedLabel)){
           return true;
        } else {
           return false;
        }
    }

    @Override
    public Object clone() {
        kNN clone = new kNN();
        clone.copy(this);
        return null;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        kNN rhs2 = (kNN)rhs;
        dataSet.clear();

        for(Map.Entry<String, String> entry : rhs2.dataSet.entrySet()){
            dataSet.put(entry.getKey(), entry.getValue());
        }
    }

    public Object getModel() {
        return dataSet;
    }
}
