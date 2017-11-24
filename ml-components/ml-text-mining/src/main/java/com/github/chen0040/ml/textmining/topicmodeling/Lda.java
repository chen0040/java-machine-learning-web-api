package com.github.chen0040.ml.textmining.topicmodeling;

import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.textmining.commons.Tuple2;
import com.github.chen0040.ml.textretrieval.filters.LowerCase;
import com.github.chen0040.ml.textretrieval.filters.StopWordRemoval;
import com.github.chen0040.ml.textretrieval.tokenizers.BasicTokenizer;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by root on 11/3/15.
 */
public class Lda extends AbstractMLModule {

    public static final String DOCUMENT_TOPIC_SMOOTHING = "documentTopicSmoothing";
    public static final String TOPIC_WORD_SMOOTHING = "topicWordSmoothing";

    // Constants for calculating topic correlation. A doc with 5% or more tokens in a topic is "about" that topic.
    public static final String CORRELATION_MIN_TOKENS = "correlationMinTokens";
    public static final String CORRELATION_MIN_PROPORTION = "correlationMinProportion";

    public static final String TOPIC_COUNT = "topicCount";

    // Use a more agressive smoothing parameter to sort
    //  documents by topic. This has the effect of preferring
    //  longer documents.
    public static final String DOC_SORT_SMOOTHING = "docSortSmoothing";

    public static final String RETAIN_RAW_DATA = "retainRawData";

    public static final String MAX_VOCABULARY_SIZE = "maxVocabularySize";

    public static final String MAX_TOPIC_SUMMARY_LENGTH = "maxTopicSummaryLength";

    public static final String MAX_SWEEP_COUNT = "maxSweepCount";

    public LdaModel model;

    private StopWordRemoval stopWordFilter;
    private LowerCase lowerCaseFilter;

    private Random random = new Random();

    public static final String REMOVE_IPADDRESS = "removeIPAddress";
    public static final String REMOVE_NUMBER = "removeNumber";

    private boolean removeNumber(){
        return getAttribute(REMOVE_NUMBER) > 0.5;
    }

    private void setRemoveNumber(boolean remove){
        setAttribute(REMOVE_NUMBER, remove ? 1 : 0);
    }

    private boolean removeIPAddress(){
        return getAttribute(REMOVE_IPADDRESS) > 0.5;
    }

    private void setRemoveIpaddress(boolean remove){
        setAttribute(REMOVE_IPADDRESS, remove ? 1 : 0);
    }

    private int maxSweepCount(){
        return (int)getAttribute(MAX_SWEEP_COUNT);
    }

    private void _maxSweepCount(int count){
        setAttribute(MAX_SWEEP_COUNT, count);
    }

    private int maxTopicSummaryLength(){
        return (int)getAttribute(MAX_TOPIC_SUMMARY_LENGTH);
    }

    public void _maxTopicSummaryLength(int val){
        setAttribute(MAX_TOPIC_SUMMARY_LENGTH, val);
    }


    private void _maxVocabularySize(int maxSize){
        setAttribute(MAX_VOCABULARY_SIZE, maxSize);
    }

    private int maxVocabularySize(){
        return (int)getAttribute(MAX_VOCABULARY_SIZE);
    }

    private boolean retainRawData(){
        return getAttribute(RETAIN_RAW_DATA) > 0.5;
    }

    private void _retainRawData(boolean state){
        setAttribute(RETAIN_RAW_DATA, state ? 1 : 0);
    }

    private double docSortSmoothing(){
        return getAttribute(DOC_SORT_SMOOTHING);
    }

    private double sumDocSortSmoothing(){
        return docSortSmoothing() * topicCount();
    }

    private void _docSortSmoothing(double value){
        setAttribute(DOC_SORT_SMOOTHING, value);
    }

    private int topicCount(){
        return (int)getAttribute(TOPIC_COUNT);
    }

    private void _topicCount(int value){
        setAttribute(TOPIC_COUNT, value);
    }

    public LdaModel getModel(){
        return model;
    }

    public void setModel(LdaModel model){
        this.model = model;
    }

    private void _correlationMinProportion(double value){
        setAttribute(CORRELATION_MIN_PROPORTION, value);
    }

    private double correlationMinProportion(){
        return getAttribute(CORRELATION_MIN_PROPORTION);
    }

    private void _correlationMinTokens(int value){
        setAttribute(CORRELATION_MIN_TOKENS, value);
    }

    private int correlationMinTokens(){
        return (int)getAttribute(CORRELATION_MIN_TOKENS);
    }

    private void _documentTopicSmoothing(double value){
        setAttribute(DOCUMENT_TOPIC_SMOOTHING, value);
    }

    private void _topicWordSmoothing(double value){
        setAttribute(TOPIC_WORD_SMOOTHING, value);
    }

    private double documentTopicSmoothing(){
        return getAttribute(DOCUMENT_TOPIC_SMOOTHING);
    }

    public double topicWordSmoothing(){
        return getAttribute(TOPIC_WORD_SMOOTHING);
    }

    public void addStopWords(List<String> stopWords){
        this.stopWordFilter.join(stopWords);
    }

    public Consumer<String> progressListener;

    public void setProgressListener(Consumer<String> listener){
        progressListener = listener;
    }

    private void notifyProgressChanged(String message){
        if(progressListener != null) {
            progressListener.accept(message);
        }
    }

    @Override
    public Object clone() {
        Lda clone = new Lda();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        Lda rhs2 = (Lda)rhs;
        this.model = (LdaModel)rhs2.model.clone();
        stopWordFilter = (StopWordRemoval)rhs2.stopWordFilter.clone();
        lowerCaseFilter = (LowerCase)rhs2.lowerCaseFilter.clone();
    }

    public Lda(){
        _documentTopicSmoothing(0.1);
        _topicWordSmoothing(0.01);

        _correlationMinTokens(2);
        _correlationMinProportion(0.05);

        _topicCount(20);
        _docSortSmoothing(10.0);

        _retainRawData(false);
        _maxVocabularySize(1000000);

        _maxSweepCount(50);
        _maxTopicSummaryLength(10);

        setRemoveIpaddress(true);
        setRemoveNumber(true);

        stopWordFilter = new StopWordRemoval();
        lowerCaseFilter = new LowerCase();
    }

    private List<Tuple2<Integer, Tuple2<List<String>, Long>>> map1(IntelliContext batch0){

        IntelliContext batch1 = (IntelliContext)batch0;
        int m = batch1.docCount();

        List<Tuple2<Integer, Tuple2<List<String>, Long>>> result = new ArrayList<>();
        for(int i=0; i < m; ++i){
            BasicDocument doc = batch1.docAtIndex(i);
            doc.getRawContents();

            List<String> words = doc.getRawContents();
            long timestamp = doc.getTimestamp();

            words = BasicTokenizer.doTokenize(words);

            words = trim(words);

            words = lowerCaseFilter.filter(words);
            words = stopWordFilter.filter(words);

            if(!words.isEmpty()){
                Tuple2<Integer, Tuple2<List<String>, Long>> tuple = new Tuple2<>(i, new Tuple2<>(words, timestamp));
                result.add(tuple);
            }
        }
        return result;
    }

    private List<String> trim(List<String> words){
        Pattern ge = Pattern.compile(",");
        List<String> processed = new ArrayList<>();
        for(int i=0; i < words.size(); ++i){
            String word = words.get(i);
            if(word.contains(",")){
                List<String> words2 = Arrays .asList(ge.split(word));
                for(int j=0; j < words2.size(); ++j) {
                    word = words2.get(j);
                    if(word.length() > 30){
                        word = word.substring(0, 30);
                    }
                    processed.add(word);
                }
            } else {
                if(word.length() > 30){
                    word = word.substring(0, 30);
                }
                processed.add(word);
            }
        }

        return processed;
    }




    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch0) {
        stopWordFilter.setRemoveNumbers(removeNumber());
        stopWordFilter.setRemoveIPAddress(removeIPAddress());

        notifyProgressChanged("Data preprocessing ...");

        List<Tuple2<Integer, Tuple2<List<String>, Long>>> batch = map1(batch0);

        notifyProgressChanged("Model building ...");

        buildModel(batch);



        int wordCount = model.wordCount();

        int topicCount = topicCount();


        double[] topicWeights = new double[topicCount];


        int docCount = batch.size();

        notifyProgressChanged("Matrix initialization ...");

        List<Doc> documents = new ArrayList<>();
        for(int docIndex = 0; docIndex < docCount; ++docIndex){
            Tuple2<List<String>, Long> doc_tt = batch.get(docIndex)._2();
            List<String> doc = doc_tt._1();
            long timestamp = doc_tt._2();

            Doc document = new Doc(topicCount);
            document.docIndex = docIndex;
            document.timestamp = timestamp;


            for(int i=0; i < doc.size(); ++i){

                String word = doc.get(i);

                int wordIndex = model.vocabulary.indexOf(word);
                int topicIndex = random.nextInt(topicCount);

                if(wordIndex==-1) continue;

                model.tokensPerTopic[topicIndex]++;

                model.wordTopicCounts[wordIndex][topicIndex]++;

                model.vocabularyCounts[wordIndex]++;
                model.topicCounts[topicIndex]++;

                document.addToken(wordIndex, topicIndex);
            }
            documents.add(document);
        }

        int sweepCount = maxSweepCount();

        notifyProgressChanged("Start iteration ...");

        for(int sweepIndex = 0; sweepIndex < sweepCount; ++sweepIndex){

            for(int docIndex=0; docIndex < docCount; ++docIndex){
                Doc currentDoc = documents.get(docIndex);
                int[] docTopicCounts = currentDoc.topicCounts;

                for (int position = 0; position < currentDoc.tokens.size(); position++) {
                    Token token = currentDoc.tokens.get(position);

                    model.tokensPerTopic[token.topicIndex]--;

                    int[] currentWordTopicCounts = model.wordTopicCounts[token.wordIndex];

                    currentWordTopicCounts[ token.topicIndex ]--;
                    docTopicCounts[ token.topicIndex ]--;

                    for (int topicIndex = 0; topicIndex < topicCount; topicIndex++) {
                        if (currentWordTopicCounts[topicIndex] > 0) {
                            topicWeights[topicIndex] =
                                    (documentTopicSmoothing() + docTopicCounts[topicIndex]) *
                                            (topicWordSmoothing() + currentWordTopicCounts[ topicIndex ]) /
                                            (wordCount * topicWordSmoothing() + model.tokensPerTopic[topicIndex]);
                        }
                        else {
                            topicWeights[topicIndex] =
                                    (documentTopicSmoothing() + docTopicCounts[topicIndex]) * topicWordSmoothing() /
                                            (wordCount * topicWordSmoothing() + model.tokensPerTopic[topicIndex]);
                        }
                    }

                    token.topicIndex = sampleDiscrete(topicWeights);

                    model.tokensPerTopic[token.topicIndex]++;
                    if (currentWordTopicCounts[ token.topicIndex ] <= 0) {
                        currentWordTopicCounts[ token.topicIndex ] = 1;
                    }
                    else {
                        currentWordTopicCounts[ token.topicIndex ] += 1;
                    }
                    docTopicCounts[ token.topicIndex ]++;
                }
            }

            notifyProgressChanged("Iterating #"+sweepIndex + " / " + sweepCount);
        }


        notifyProgressChanged("Finalizing ...");

        model.sortTopicWords();
        model.createTopicSummary(maxTopicSummaryLength());

        LdaBatchUpdateResult result = new LdaBatchUpdateResult(model, documents);

        notifyProgressChanged("Completed!");

        return result;
    }

    public int sampleDiscrete (double[] weights) {
        double sample = sum(weights) * Math.random();
        int i = 0;
        sample -= weights[i];
        while (sample > 0.0) {
            i++;
            sample -= weights[i];
        }
        return i;
    }

    private double sum(double[] weights){
        double sum = 0;
        for(int i=0; i < weights.length; ++i){
            sum += weights[i];
        }
        return sum;
    }

    private List<Tuple2<String, Integer>> map2(List<Tuple2<Integer, Tuple2<List<String>, Long>>> batch){
        Map<String, Integer> wordCounts = new HashMap<>();

        String word;
        for(int i=0; i < batch.size(); ++i){
            Tuple2<Integer, Tuple2<List<String>, Long>> t = batch.get(i);
            List<String> t2 = t._2()._1();
            for(int j=0; j < t2.size(); ++j){
                word = t2.get(j);

                if(wordCounts.containsKey(word)){
                    wordCounts.put(word, wordCounts.get(word)+1);
                } else {
                    wordCounts.put(word, 1);
                }
            }
        }

        List<Tuple2<String, Integer>> result = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : wordCounts.entrySet()){
            result.add(new Tuple2<>(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void buildModel(List<Tuple2<Integer, Tuple2<List<String>, Long>>> batch){
        model = new LdaModel();

        model.topicCount = topicCount();
        model.documentTopicSmoothing = documentTopicSmoothing();
        model.topicWordSmoothing = topicWordSmoothing();
        model.correlationMinTokens = correlationMinTokens();
        model.correlationMinProportion = correlationMinProportion();
        model.docSortSmoothing = docSortSmoothing();
        model.retainRawData = retainRawData();
        model.maxVocabularySize = maxVocabularySize();

        List<Tuple2<String, Integer>> batch3 = map2(batch);

        List<String> candidates = new ArrayList<>();
        long count = batch3.size();

        if(model.maxVocabularySize < count) {
            //sort descendingly
            batch3.sort((t1, t2)->{
                int f1 = t1._2();
                int f2 = t2._2();

                if (f1 > f2) return -1;
                else if (f1 == f2) return 0;
                else return 1;
            });
            for(int i=0; i < model.maxVocabularySize; ++i){
                candidates.add(batch3.get(i)._1());
            }
        } else {
            for(int i=0; i < count; ++i){
                candidates.add(batch3.get(i)._1());
            }
        }

        System.out.println("Vocabulary Size: "+candidates.size());

        model.initialize(candidates);
    }


    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        return 0;
    }
}
