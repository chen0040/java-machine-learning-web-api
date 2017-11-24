package com.github.chen0040.ml.spark.text.topicmodeling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.text.Vocabulary;
import com.github.chen0040.ml.spark.text.filters.LowerCase;
import com.github.chen0040.ml.spark.utils.views.AlgorithmViewTimePoint;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.text.filters.StopWordRemoval;
import com.github.chen0040.ml.spark.utils.views.AlgorithmViewTimeSeries;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by root on 11/3/15.
 */
public class Lda extends AbstractSparkMLModule {

    private LdaModel model;

    private StopWordRemoval stopWordFilter;
    private LowerCase lowerCaseFilter;

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

    public static final String REMOVE_IPADDRESS = "removeIPAddress";
    public static final String REMOVE_NUMBER = "removeNumber";

    public static final String USE_CACHE = "useCache";

    private boolean removeNumber(){
        return getAttribute(REMOVE_NUMBER) > 0.5;
    }

    private void _removeNumber(boolean remove){
        setAttribute(REMOVE_NUMBER, remove ? 1 : 0);
    }

    private boolean removeIPAddress(){
        return getAttribute(REMOVE_IPADDRESS) > 0.5;
    }

    private void _removeIpaddress(boolean remove){
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

    private double docSortSmoothing(){
        return (Double)getAttribute(DOC_SORT_SMOOTHING);
    }

    private boolean retainRawData(){
        return getAttribute(RETAIN_RAW_DATA) > 0.5;
    }

    private void _retainRawData(boolean state){
        setAttribute(RETAIN_RAW_DATA, state ? 1 : 0);
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

    private void _useCache(boolean value){
        setAttribute(USE_CACHE, value ? 1 : 0);
    }

    private boolean useCache(){
        return getAttribute(USE_CACHE) > 0.5;
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
        stopWordFilter.join(stopWords);
    }

    @Override
    public Object clone() {
        Lda clone = new Lda();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
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

        _removeIpaddress(true);
        _removeNumber(true);

        _useCache(true);

        stopWordFilter = new StopWordRemoval();
        lowerCaseFilter = new LowerCase();
    }


    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch0){
        stopWordFilter.setRemoveIPAddress(removeIPAddress());
        stopWordFilter.setRemoveNumbers(removeNumber());
        boolean useCache = useCache();

        JavaPairRDD<SparkMLTuple, List<String>> lsRdd = batch0.mapToPair(tuple -> {
            List<String> words = tuple.toBagOfWords();

            Pattern ge = Pattern.compile(",");
            List<String> processed = new ArrayList<>();
            for(int i=0; i < words.size(); ++i){
                String word = words.get(i);
                if(word.contains(",")){
                    List<String> words2 = Arrays.asList(ge.split(word));
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

            words = processed;

            words = lowerCaseFilter.filter(words);
            words = stopWordFilter.filter(words);
            return new Tuple2<>(tuple, words);
        }).filter(t -> !t._2().isEmpty());

        buildModel(lsRdd);

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(batch0.context());

        int wordCount_global = model.wordCount();
        Broadcast<Integer> wordCount_bc = sc.broadcast(wordCount_global);
        Broadcast<Double> topicWordSmoothing_bc = sc.broadcast(topicWordSmoothing());
        Broadcast<Double> documentTopicSmoothing_bc = sc.broadcast(documentTopicSmoothing());
        Broadcast<Vocabulary> vocabulary_bc = sc.broadcast(model.vocabulary);
        Broadcast<Boolean> retainRawData_bc = sc.broadcast(retainRawData());

        int topicCount_global = topicCount();
        Broadcast<Integer> topicCount_bc = sc.broadcast(topicCount_global);

        Accumulator<Integer>[] tokensPerTopic_acc = new Accumulator[topicCount_global];
        Accumulator<Integer>[][] wordTopicCounts_acc = new Accumulator[wordCount_global][];
        Accumulator<Integer>[] vocabularyCounts_acc = new Accumulator[wordCount_global];
        Accumulator<Integer>[] topicCounts_acc = new Accumulator[topicCount_global];

        for(int i=0; i < topicCount_global; ++i){
            tokensPerTopic_acc[i] = sc.accumulator(0, new NonNegativeIntAccumulatorParam());
        }
        for(int i=0; i < wordCount_global; ++i){
            wordTopicCounts_acc[i] = new Accumulator[topicCount_global];
            for(int j=0; j < topicCount_global; ++j){
                wordTopicCounts_acc[i][j] = sc.accumulator(0, new NonNegativeIntAccumulatorParam());
            }
        }
        for(int i=0; i < wordCount_global; ++i){
            vocabularyCounts_acc[i] = sc.accumulator(0, new NonNegativeIntAccumulatorParam());
        }
        for(int i=0; i < topicCount_global; ++i){
            topicCounts_acc[i] = sc.accumulator(0, new NonNegativeIntAccumulatorParam());
        }


        JavaRDD<Doc> docRdd = lsRdd.map(tt->{
            int topicCount_local = topicCount_bc.value();

            Doc document = new Doc(topicCount_local);
            List<String> list = tt._2();

            boolean retainRawData_local = retainRawData_bc.value();

            document.timestamp = tt._1().getTimestamp();

            if(retainRawData_local) {
                document.content = tt._1();
            }

            Vocabulary vocabulary_local = vocabulary_bc.value();
            Random random = new Random();

            for(String word : list){
                int wordIndex = vocabulary_local.indexOf(word);
                int topicIndex = random.nextInt(topicCount_local);

                if(wordIndex == -1) continue;

                document.addToken(wordIndex, topicIndex);
            }
            return document;
        });

        if(useCache){
            docRdd.cache();
        }

        // accumulator works only in action method, in particular foreach method
        docRdd.foreach(doc->{
            List<Token> tokens = doc.tokens;
            int tokenCount = tokens.size();
            int topicIndex = -1;
            int wordIndex = -1;
            Token token;

            for(int i=0; i < tokenCount; ++i){
                token = doc.tokens.get(i);
                topicIndex = token.topicIndex;
                wordIndex = token.wordIndex;

                tokensPerTopic_acc[topicIndex].add(1);
                wordTopicCounts_acc[wordIndex][topicIndex].add(1);
                vocabularyCounts_acc[wordIndex].add(1);
                topicCounts_acc[topicIndex].add(1);
            }
        });

        int sweepCount = maxSweepCount();

        System.out.println("document count: " + docRdd.count());

        for(int sweepIndex = 0; sweepIndex < sweepCount; ++sweepIndex){

            System.out.println("Sweep #"+sweepIndex);



            int[][] wordTopicCounts_bc_temp = new int[wordCount_global][];
            for(int i=0; i < wordCount_global; ++i){
                wordTopicCounts_bc_temp[i] = new int[topicCount_global];
                for(int j=0; j < topicCount_global; ++j){
                    wordTopicCounts_bc_temp[i][j] = wordTopicCounts_acc[i][j].value();
                }
            }
            Broadcast<int[][]> wordTopicCounts_bc = sc.broadcast(wordTopicCounts_bc_temp);

            int[] tokensPerTopic_bc_temp = new int[topicCount_global];
            for(int i=0; i < topicCount_global; ++i){
                tokensPerTopic_bc_temp[i] = tokensPerTopic_acc[i].value();
            }
            Broadcast<int[]> tokensPerTopic_bc = sc.broadcast(tokensPerTopic_bc_temp);

                    System.out.println("Sweep #"+sweepIndex+" broadcast completed!");

            docRdd = docRdd.map(doc->{
                int[] docTopicCounts = doc.topicCounts;
                int topicCount_local = topicCount_bc.value();
                int wordCount_local = wordCount_bc.value();
                double topicWordSmoothing_local = topicWordSmoothing_bc.value();
                double documentTopicSmoothing_local = documentTopicSmoothing_bc.value();

                int[][] wordTopicCounts_local = wordTopicCounts_bc.value();
                int[] tokensPerTopic_local = tokensPerTopic_bc.value();

                List<Token> tokens = doc.tokens;
                Token token = null;
                int tokenCount = tokens.size();
                for (int position = 0; position < tokenCount; position++) {
                    token = tokens.get(position);

                    int[] wordTopicCountsByWord_local = wordTopicCounts_local[token.wordIndex];

                    double[] topicWeights = new double[topicCount_local];
                    for (int topicIndex = 0; topicIndex < topicCount_local; topicIndex++) {

                        int cwt = wordTopicCountsByWord_local[topicIndex];

                        topicWeights[topicIndex] = (documentTopicSmoothing_local + docTopicCounts[topicIndex]) *
                                (topicWordSmoothing_local + cwt) /
                                (wordCount_local * topicWordSmoothing_local + tokensPerTopic_local[topicIndex]);
                    }

                    docTopicCounts[token.topicIndex]--;

                    token.prevTopicIndex = token.topicIndex;

                    //sample for new topic
                    double sample = sum(topicWeights) * Math.random();
                    int selectedTopicIndex = 0;
                    sample -= topicWeights[selectedTopicIndex];
                    while (sample > 0.0) {
                        selectedTopicIndex++;
                        sample -= topicWeights[selectedTopicIndex];
                    }
                    token.topicIndex = selectedTopicIndex;

                    docTopicCounts[token.topicIndex]++;
                }

                return doc;
            });

            docRdd.foreach(doc->{
                List<Token> tokens = doc.tokens;
                int tokenCount = tokens.size();
                Token token = null;
                int prevTopicIndex = -1;
                int topicIndex = -1;
                int wordIndex = -1;

                for (int position = 0; position < tokenCount; position++) {
                    token = tokens.get(position);
                    prevTopicIndex = token.prevTopicIndex;
                    topicIndex = token.topicIndex;
                    wordIndex = token.wordIndex;

                    Accumulator[] currentWordTopicCounts = wordTopicCounts_acc[wordIndex];

                    tokensPerTopic_acc[prevTopicIndex].add(-1);
                    currentWordTopicCounts[prevTopicIndex].add(-1);

                    tokensPerTopic_acc[topicIndex].add(1);
                    currentWordTopicCounts[topicIndex].add(1);
                }
            });

            System.out.println("Sweep #" + sweepIndex + " m1");

            wordTopicCounts_bc.unpersist();
            tokensPerTopic_bc.unpersist();

            for(int i=0; i < topicCount_global; ++i){
                model.tokensPerTopic[i] = tokensPerTopic_acc[i].value();
            }
            for(int i=0; i < wordCount_global; ++i){
                for(int j=0; j < topicCount_global; ++j){
                    model.wordTopicCounts[i][j] = wordTopicCounts_acc[i][j].value();
                }
            }
            for(int i=0; i < wordCount_global; ++i){
                model.vocabularyCounts[i] = vocabularyCounts_acc[i].value();
            }
            for(int i=0; i < topicCount_global; ++i){
                model.topicCounts[i] = topicCounts_acc[i].value();
            }
        }

        model.sortTopicWords();
        model.createTopicSummary(maxTopicSummaryLength());

        LdaBatchUpdateResult result = new LdaBatchUpdateResult(model, docRdd);
        return result;
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


    @Override
    public PredictionResult evaluate(SparkMLTuple tuple, JavaSparkContext sc) {
        JavaRDD<SparkMLTuple> batch = convertToBatch(tuple, sc);

        LdaBatchUpdateResult result = (LdaBatchUpdateResult)batchUpdate(batch);

        int topicCount = result.topicCount();
        long docCount = result.docCount();


        List<AlgorithmViewTimeSeries> docTimeSeriesList = new ArrayList<>();
        for(int topicIndex = 0; topicIndex < topicCount; ++topicIndex){
            AlgorithmViewTimeSeries docTimeSeries = new AlgorithmViewTimeSeries();
            docTimeSeries.setName(result.topicSummary(topicIndex));

            List<Double> probability_doc = result.scoreDocuments(topicIndex).map(tt -> tt._1()).collect();
            for(int docIndex = 0; docIndex < docCount; ++docIndex){
                docTimeSeries.getData().add(new AlgorithmViewTimePoint(topicIndex, probability_doc.get(docIndex)));
            }
        }

        double[][] correlation = result.getTopicCorrelations();

        PredictionResult predictedValue = new PredictionResult();

        predictedValue.addFeature("topicCount", ""+topicCount);
        predictedValue.addFeature("docCount", ""+docCount);
        predictedValue.addFeature("timeseries", JSON.toJSONString(docTimeSeriesList, SerializerFeature.BrowserCompatible));
        predictedValue.addFeature("correlations", JSON.toJSONString(correlation, SerializerFeature.BrowserCompatible));

        return predictedValue;
    }


    private double sum(double[] weights){
        double sum = 0;
        for(int i=0; i < weights.length; ++i){
            sum += weights[i];
        }
        return sum;
    }


    private void buildModel(JavaPairRDD<SparkMLTuple, List<String>> batch){
        model = new LdaModel();

        model.topicCount = topicCount();
        model.documentTopicSmoothing = documentTopicSmoothing();
        model.topicWordSmoothing = topicWordSmoothing();
        model.correlationMinTokens = correlationMinTokens();
        model.correlationMinProportion = correlationMinProportion();
        model.docSortSmoothing = docSortSmoothing();
        model.retainRawData = retainRawData();
        model.maxVocabularySize = maxVocabularySize();

        JavaPairRDD<String, Integer> batch2 = batch.flatMap(list->list._2()).mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> batch3 = batch2.reduceByKey((i1, i2) -> i1 + i2);

        List<Tuple2<String, Integer>> words = null;

        long count = batch3.count();

        if(model.maxVocabularySize < count) {
            words = batch3.top(model.maxVocabularySize, new WordComparator());
        } else {
            words = batch3.collect();
        }

        System.out.println("Vocabulary Size: "+words.size());

        List<String> candidates = new ArrayList<>();
        for(int i=0; i < words.size(); ++i){
            candidates.add(words.get(i)._1());
        }

        model.initialize(candidates);
    }


    private static class WordComparator implements Comparator<Tuple2<String, Integer>>, Serializable {

        @Override
        public int compare(Tuple2<String, Integer> t1, Tuple2<String, Integer> t2) {
            int f1 = t1._2();
            int f2 = t2._2();

            if (f1 > f2) return -1;
            else if (f1 == f2) return 0;
            else return 1;
        }
    }



}
