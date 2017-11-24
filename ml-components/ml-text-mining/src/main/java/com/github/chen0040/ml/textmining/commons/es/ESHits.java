package com.github.chen0040.ml.textmining.commons.es;

import com.github.chen0040.ml.textretrieval.tokenizers.BasicTokenizer;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by root on 9/7/15.
 */
public class ESHits {
    public int total;
    public double max_score;
    public ArrayList<ESHit> hits = new ArrayList<ESHit>();

    private static Logger logger = Logger.getLogger(String.valueOf(ESHits.class));

    public void add(ESHit hit){
        hits.add(hit);
        total++;
    }

    public void sortAscendinglyByTime(){
        Collections.sort(hits, new Comparator<ESHit>() {
            public int compare(ESHit h1, ESHit h2) {
                return Long.compare(h1._source.Date, h2._source.Date);
            }
        });
    }

    public List<String> getUniqueWords(){
        BasicTokenizer tokenizer = new BasicTokenizer();
        HashSet<String> uniqueWords = new HashSet<>();
        for(int i=0; i < hits.size(); ++i) {
            ESHit hit = hits.get(i);
            String text = hit._source.Message;
            List<String> words = tokenizer.tokenize(text);
            uniqueWords.addAll(words);
        }
        List<String> result = new ArrayList<>();
        for(String word : uniqueWords){
            result.add(word);
        }
        return result;
    }

    public ArrayList<String> rawContents(){
        ArrayList<String> lines = new ArrayList<>();
        for(int i=0; i < hits.size(); ++i){
            ESHit hit = hits.get(i);
            lines.add(hit._source.Message);
        }
        return lines;
    }

    public HashMap<String, Integer> getWordCounts(){
        BasicTokenizer tokenizer = new BasicTokenizer();
        HashMap<String, Integer> uniqueWords = new HashMap<String, Integer>();
        for(int i=0; i < hits.size(); ++i) {
            ESHit hit = hits.get(i);
            String text = hit._source.Message;
            List<String> words = tokenizer.tokenize(text);
            for(String word : words){
                if(uniqueWords.containsKey(word)){
                    uniqueWords.put(word, uniqueWords.get(word)+1);
                }else{
                    uniqueWords.put(word, 1);
                }
            }
        }
        return uniqueWords;
    }

    public ESTimeWindowDoc reduceBySeverityKeywords(List<String> severitiesTotrack){
        ESTimeWindowDoc doc = new ESTimeWindowDoc();
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        for(int i=0; i < hits.size(); ++i){
            ESHit hit = hits.get(i);
            doc.addRawContent(hit._source.Message);
            for(int j=0; j < severitiesTotrack.size(); ++j){
                String keyword = severitiesTotrack.get(j).trim();
                int count = hit.getSeverityCount(keyword);

                if(doc.containsKey(keyword)){
                    doc.put(keyword, count + doc.get(keyword));
                }else {
                    doc.put(keyword, (double) count);
                }
            }
            String label = hit._source.Severity;
            if(counts.containsKey(label)){
                counts.put(label, counts.get(label)+1);
            }else{
                counts.put(label, 1);
            }
        }

        int maxCount = 0;
        String selectedLabel = "";
        for(String label : counts.keySet()){
            int count = counts.get(label);
            if(count > maxCount){
                maxCount = count;
                selectedLabel = label;
            }
        }

        doc.label = selectedLabel;

        return doc;
    }



    public ESTimeWindowDoc reduceByRegex(List<String> regexList){
        ESTimeWindowDoc doc = new ESTimeWindowDoc();
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        for(int i=0; i < hits.size(); ++i){
            ESHit hit = hits.get(i);
            doc.addRawContent(hit._source.Message);
            for(int j=0; j < regexList.size(); ++j){
                String keyword = regexList.get(j).trim();
                int count = hit.getCountByRegexMatch(keyword);

                if(doc.containsKey(keyword)){
                    doc.put(keyword, count + doc.get(keyword));
                }else {
                    doc.put(keyword, (double) count);
                }
            }
            String label = hit._source.Severity;
            if(counts.containsKey(label)){
                counts.put(label, counts.get(label)+1);
            }else{
                counts.put(label, 1);
            }
        }

        int maxCount = 0;
        String selectedLabel = "";
        for(String label : counts.keySet()){
            int count = counts.get(label);
            if(count > maxCount){
                maxCount = count;
                selectedLabel = label;
            }
        }

        doc.label = selectedLabel;

        return doc;
    }



    public Map<ESTimeWindowKey, ESHits> flatMapByTimeWindow(int timeWindowSize){
        sortAscendinglyByTime();
        if(hits.isEmpty()) return new HashMap<>();

        long startTime = hits.get(0)._source.Date;
        long endTime = hits.get(hits.size()-1)._source.Date;

        //logger.info("hits Count: "+hits.size());
        //logger.info("startTime: "+startTime);
        //logger.info("endTime: "+endTime);
        //logger.info("timeWindowSize: "+timeWindowSize);

        int index=0;
        HashMap<ESTimeWindowKey, ESHits> result = new HashMap<ESTimeWindowKey, ESHits>();
        for(long t = startTime; t < endTime; t+=timeWindowSize){

            long windowStart = t;
            long windowEnd = t+timeWindowSize;
            while(between(hits.get(index)._source.Date, windowStart, windowEnd)){
                add(result, hits.get(index), windowStart, windowEnd);
                index++;
                if(index+1 >= hits.size()) break;
            }

            if(index+1 >= hits.size()) break;

        }

        return result;

    }

    private static boolean between(long d, long t1, long t2){
        return(d >= t1 && d < t2);
    }

    private static void add(Map<ESTimeWindowKey, ESHits> result, ESHit hit, long startTime, long endTime){
        ESTimeWindowKey key = new ESTimeWindowKey(startTime, endTime);
        if(result.containsKey(key)){
            result.get(key).add(hit);
        }else{
            ESHits hits2 = new ESHits();
            hits2.add(hit);
            result.put(key, hits2);
        }
    }


}
