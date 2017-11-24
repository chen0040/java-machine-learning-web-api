package com.github.chen0040.ml.spark.recommender;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created by root on 9/14/15.
 * Algorithm which implements the "Customers who bought this item also bought ..." (CWBTIAB)
 * Input: item A and customer H
 * Output: a list of (Top N) items frequently bought by the customer H who also bought item A at the same time
 */
public class CWBTIAB extends AbstractSparkMLModule {

    private String customerId; // id of customer H
    private String itemId; // id of item A
    private int topN = 5;
    private List<Tuple2<String, Integer>> topNItems = new ArrayList<Tuple2<String, Integer>>();

    @Override
    public Object clone(){
        CWBTIAB clone = new CWBTIAB();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        CWBTIAB rhs2 = (CWBTIAB) rhs;
        customerId = rhs2.customerId;
        itemId = rhs2.itemId;
        topN = rhs2.topN;
        topNItems.clear();

        for(int i=0; i < rhs2.topNItems.size(); ++i){
            Tuple2<String, Integer> item = rhs2.topNItems.get(i);
            topNItems.add(new Tuple2<String, Integer>(item._1(), item._2()));
        }
    }

    public List<Tuple2<String, Integer>> getTopNItems() {
        return topNItems;
    }

    public void setTopNItems(List<Tuple2<String, Integer>> topNItems) {
        this.topNItems = topNItems;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public String getCustomerId(){
        return customerId;
    }

    public void setCustomerId(String customerId){
        this.customerId = customerId;
    }

    public String getItemId(){
        return itemId;
    }

    public void setItemId(String itemId){
        this.itemId = itemId;
    }

    // each SparkMLTuple stores the items purchase by customer H in its
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch){

        JavaRDD<SparkMLTuple> filteredBatch = batch.filter(new Function<SparkMLTuple, Boolean>() {
            public Boolean call(SparkMLTuple transaction) throws Exception {
                return transaction.getLabelOutput().equals(customerId) && transaction.toBagOfWords().contains(itemId);
            }
        });

        JavaPairRDD<String, Integer> rdd1 = filteredBatch.flatMapToPair(new PairFlatMapFunction<SparkMLTuple, String, Integer>() {
            public Iterable<Tuple2<String, Integer>> call(SparkMLTuple transaction) throws Exception {
                List<String> items = transaction.toBagOfWords();
                HashSet<String> uniqueItems = new HashSet<>();
                for (int i = 0; i < items.size(); ++i) {
                    uniqueItems.add(items.get(i));
                }
                List<Tuple2<String, Integer>> result = new ArrayList<Tuple2<String, Integer>>();
                for (String item : uniqueItems) {
                    result.add(new Tuple2<String, Integer>(item, 1));
                }
                return result;
            }
        });

        JavaPairRDD<String, Integer> rdd2 = rdd1.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        topNItems = rdd2.top(topN, new Comp());


        return new BatchUpdateResult();
    }

    private class Comp implements Comparator<Tuple2<String, Integer>>, Serializable {
        public int compare(Tuple2<String, Integer> t1, Tuple2<String, Integer> t2) {
            return Integer.compare(t1._2(), t2._2());
        }
    }
}
