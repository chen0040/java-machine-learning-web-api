package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/4/15.
 */
public class LdaBatchUpdateResult extends BatchUpdateResult {
    private LdaModel model;
    private JavaRDD<Doc> documents;
    private long docCount;

    public LdaBatchUpdateResult(LdaModel model, JavaRDD<Doc> documents){
        this.model = model;
        this.documents = documents;
        this.docCount = documents.count();
    }

    public JavaRDD<Doc> documents(){
        return documents;
    }

    public long docCount() { return docCount; }

    public JavaPairRDD<Double, Doc> scoreDocuments(int selectedTopicIndex){
        if (selectedTopicIndex == -1) {
            return null;
        }

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(documents.context());

        Broadcast<Double> sumDocSortSmoothing_bc = sc.broadcast(model.sumDocSortSmoothing());
        Broadcast<Double> docSortSmoothing_bc = sc.broadcast(model.docSortSmoothing);


        JavaPairRDD<Double, Doc> scores = documents.mapToPair(doc -> {

            double docSortSmoothing_local = docSortSmoothing_bc.value();
            double sumDocSortSmoothing_local = sumDocSortSmoothing_bc.value();

            double score = (doc.topicCounts[selectedTopicIndex] + docSortSmoothing_local) / (doc.tokens.size() + sumDocSortSmoothing_local);

            return new Tuple2<>(score, doc);
        });

        return scores;
    }

    public String topicSummary(int selectedTopicIndex){
        return topicSummary(selectedTopicIndex, 3);
    }

    public String topicSummary(int selectedTopicIndex, int length){
        return model.topicSummaries[selectedTopicIndex];
    }

    public int topicCount(){
        return model.topicCount;
    }

    public JavaPairRDD<Double, Doc> reorderDocuments(int selectedTopicIndex) {

        if(selectedTopicIndex==-1) return null;

        return scoreDocuments(selectedTopicIndex).sortByKey(false); //sort descendingly
    }

    public List<VocabularyTableCell> vocabularyTable(int size){
        return model.vocabularyTable(size);
    }

    public double[][] getTopicCorrelations() {
        int topicCount = model.topicCount;

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(documents.context());

        Accumulator<Integer>[] topicProbabilities_acc = new Accumulator[topicCount];
        for(int topicIndex = 0; topicIndex < topicCount; ++topicIndex){
            topicProbabilities_acc[topicIndex] = sc.accumulator(0);
        }

        // initialize the matrix
        Accumulator<Integer>[][] correlationMatrix_acc = new Accumulator[topicCount][];
        for (int topicIndex = 0; topicIndex < topicCount; topicIndex++) {
            correlationMatrix_acc[topicIndex] = new Accumulator[topicCount];
            for(int topicIndex2 = 0; topicIndex2 < topicCount; topicIndex2++){
                correlationMatrix_acc[topicIndex][topicIndex2] = sc.accumulator(0);
            }
        }

        Broadcast<Double> correlationMinTokens_bc = sc.broadcast(model.correlationMinTokens);
        Broadcast<Double> correlationMinProportion_bc = sc.broadcast(model.correlationMinProportion);

        // iterate once to get mean log topic proportions
        documents.foreach(doc-> {

            double correlationMinTokens_local = correlationMinTokens_bc.value();
            double correlationMinProportion_local = correlationMinProportion_bc.value();

            // We want to find the subset of topics that occur with non-trivial concentration in this document.
            // Only consider topics with at least the minimum number of tokens that are at least 5% of the doc.
            List<Integer> documentTopics = new ArrayList<>();
            double tokenCutoff = Math.max(correlationMinTokens_local, correlationMinProportion_local * doc.tokens.size());

            for (int topicIndex = 0; topicIndex < topicCount; topicIndex++) {
                if (doc.topicCounts[topicIndex] >= tokenCutoff) {
                    documentTopics.add(topicIndex);
                    topicProbabilities_acc[topicIndex].add(1); // Count the number of docs with this topic
                }
            }

            // Look at all pairs of topics that occur in the document.
            for (int i = 0; i < documentTopics.size() - 1; i++) {
                for (int j = i + 1; j < documentTopics.size(); j++) {
                    correlationMatrix_acc[documentTopics.get(i)][documentTopics.get(j)].add(1);
                    correlationMatrix_acc[documentTopics.get(j)][documentTopics.get(i)].add(1);
                }
            }
        });

        double[][] correlationMatrix = new double[topicCount][];
        for(int i=0; i < topicCount; ++i){
            correlationMatrix[i] = new double[topicCount];
        }

        for (int t1 = 0; t1 < topicCount - 1; t1++) {
            for (int t2 = t1 + 1; t2 < topicCount; t2++) {
                double denom = topicProbabilities_acc[t1].value() * topicProbabilities_acc[t2].value();
                double num = (docCount * correlationMatrix_acc[t1][t2].value());
                double value = num / denom;

                //System.out.println("value: "+value);
                correlationMatrix[t1][t2] = value == 0 ? 0 : Math.log(value);
                correlationMatrix[t2][t1] = value == 0 ? 0 : Math.log(value);
            }
        }

        return correlationMatrix;
    }

}
