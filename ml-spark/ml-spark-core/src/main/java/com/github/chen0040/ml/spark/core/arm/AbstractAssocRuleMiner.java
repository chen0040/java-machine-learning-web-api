package com.github.chen0040.ml.spark.core.arm;

import com.github.chen0040.ml.spark.core.SparkMLOutputType;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.util.*;

/**
 * Created by chen0469 on 8/19/2015 0019.
 */
public abstract class AbstractAssocRuleMiner extends AbstractSparkMLModule implements AssocRuleMiner {
    protected int minSupportLevel;
    protected int maxFrequentItemSetSize;
    protected List<String> uniqueItems;
    protected ItemSetWarehouse warehouse;


    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        AbstractAssocRuleMiner rhs2 = (AbstractAssocRuleMiner)rhs;
        minSupportLevel = rhs2.minSupportLevel;
        maxFrequentItemSetSize = rhs2.maxFrequentItemSetSize;
        uniqueItems = new ArrayList<>();
        for(String item : rhs2.uniqueItems){
            uniqueItems.add(item);
        }
        warehouse = (ItemSetWarehouse)rhs2.warehouse.clone();
    }

    public AbstractAssocRuleMiner(){
        minSupportLevel = 4;
        maxFrequentItemSetSize = -1;
        warehouse = new ItemSetWarehouse();
        setOutputType(SparkMLOutputType.MinedAssociationRules);
    }

    public int getMinSupportLevel() {
        return minSupportLevel;
    }

    public void setMinSupportLevel(int minSupportLevel) {
        this.minSupportLevel = minSupportLevel;
    }

    public int getMaxFrequentItemSetSize() {
        return maxFrequentItemSetSize;
    }

    public void setMaxFrequentItemSetSize(int maxFrequentItemSetSize) {
        this.maxFrequentItemSetSize = maxFrequentItemSetSize;
    }

    public List<String> scan4UniqueItems(JavaRDD<SparkMLTuple> batch){
        JavaRDD<String> rdd1 = batch.flatMap(new FlatMapFunction<SparkMLTuple, String>() {
            public Iterable<String> call(SparkMLTuple line) throws Exception {
                Set<String> set = new HashSet<>();
                for (String token : line.toBagOfWords()) {
                    set.add(token);
                }
                return set;
            }
        });

        JavaRDD<String> rdd2 = rdd1.distinct();

        List<String> output = rdd2.collect();
        Collections.sort(output);
        return output;
    }

    public List<String> getUniqueItems(){
        return uniqueItems;
    }


    protected void updateItemSupport(JavaRDD<Iterable<String>> batch, final List<ItemSet> C){
        for(int j=0; j < C.size(); ++j){
            C.get(j).support = 0;
        }


        JavaPairRDD<Integer, Integer> rdd = batch.flatMapToPair(new PairFlatMapFunction<Iterable<String>, Integer, Integer>() {
            public Iterable<Tuple2<Integer, Integer>> call(Iterable<String> transaction) throws Exception {
                HashMap<Integer, Integer> imap = new HashMap<Integer, Integer>();
                for (int i = 0; i < C.size(); ++i) {
                    ItemSet itemset = C.get(i);
                    if (itemset.isInTransaction(transaction)) {
                        if (imap.containsKey(i)) {
                            imap.put(i, imap.get(i) + 1);
                        } else {
                            imap.put(i, 1);
                        }
                    }
                }

                List<Tuple2<Integer, Integer>> result = new ArrayList<Tuple2<Integer, Integer>>();
                for (Integer i : imap.keySet()) {
                    result.add(new Tuple2<Integer, Integer>(i, imap.get(i)));
                }
                return result;
            }
        });

        JavaPairRDD<Integer, Integer> rdd2 = rdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        Map<Integer, Integer> items = rdd2.collectAsMap();

        for(Integer i : items.keySet()) {

            C.get(i).support+=items.get(i);
        }

    }

    protected ItemSetCluster getFrequentItemSets(List<ItemSet> C, int k){
        ItemSetCluster frequent_itemset_cluster = new ItemSetCluster(k);
        for(ItemSet itemset : C){
            if(itemset.support >= minSupportLevel){
                frequent_itemset_cluster.add(itemset);
            }
        }
        return frequent_itemset_cluster;
    }

    public ItemSetCluster scan4OneItemFrequentItemSets(JavaRDD<Iterable<String>> batch){


        final HashMap<String, ItemSet> one_item_frequent_itemset_candidates = new HashMap<String, ItemSet>();
        for(int i=0; i < uniqueItems.size(); ++i){
            one_item_frequent_itemset_candidates.put(uniqueItems.get(i), new ItemSet(uniqueItems.get(i)));
        }

        JavaPairRDD<String, Integer> rdd = batch.flatMapToPair(new PairFlatMapFunction<Iterable<String>, String, Integer>() {
            public Iterable<Tuple2<String, Integer>> call(Iterable<String> line) throws Exception {
                HashMap<String, Integer> imap = new HashMap<String, Integer>();
                for(String item : line) {
                    if(imap.containsKey(item)) {
                        imap.put(item, imap.get(item)+1);
                    }else {
                        imap.put(item, 1);
                    }
                }
                List<Tuple2<String, Integer>> result = new ArrayList<Tuple2<String, Integer>>();
                for(String item : imap.keySet()) {
                    result.add(new Tuple2<String, Integer>(item, imap.get(item)));
                }
                return result;
            }
        });

        JavaPairRDD<String, Integer> rdd2 = rdd.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1+i2;
            }
        });

        Map<String, Integer> items = rdd2.collectAsMap();

        for(String item : items.keySet()) {
            one_item_frequent_itemset_candidates.get(item).support+=items.get(item);
        }

        int k = 1;
        ItemSetCluster one_item_frequent_itemset_cluster = new ItemSetCluster(k);
        for(ItemSet itemset : one_item_frequent_itemset_candidates.values()){
            if(itemset.support >= minSupportLevel){
                one_item_frequent_itemset_cluster.add(itemset);
            }
        }

        return one_item_frequent_itemset_cluster;
    }

    public ItemSetWarehouse getFrequentItemSets(){
        return this.warehouse;
    }
}
