package com.github.chen0040.ml.tests.spark.basic;

import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created by memeanalytics on 11/9/15.
 */
public class SelectTopNTest implements Serializable {

    static class Comparator1 implements Comparator<Tuple2<String, Integer>>, Serializable {

        public static Comparator1 INSTANCE = new Comparator1();

        public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
            return Integer.compare(o1._2, o2._2);
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    static class Comparator2 implements Comparator<Tuple2<String, Integer>>, Serializable {

        public static Comparator2 INSTANCE = new Comparator2();

        public int compare(Tuple2<String, Integer> o1, Tuple2<String, Integer> o2) {
            return -Integer.compare(o1._2, o2._2);
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    @Test
    public void testTop(){
        SparkConf conf = new SparkConf();
        conf.setAppName("SelectTop2NonUnique");
        conf.setMaster("local");

        JavaSparkContext context = new JavaSparkContext(conf);
        final int N = 2;

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("top.n.input.txt").getAbsolutePath());

        JavaRDD<String> rdd = lines.repartition(9);

        JavaPairRDD<String, Integer> kv = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] tokens = s.split(",");
                return new Tuple2<String, Integer>(tokens[0], Integer.parseInt(tokens[1]));
            }
        });

        JavaPairRDD<String, Integer> uniqueKeys = kv.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> finalTopN = uniqueKeys.top(N, Comparator1.INSTANCE);

        for(int i=0; i < finalTopN.size(); ++i){
            Tuple2<String, Integer> t = finalTopN.get(i);
            System.out.println(t._1 + " = " + t._2);
        }

        context.close();
        //System.exit(0);
    }

    @Test
    public void testTakeOrdered(){
        SparkConf conf = new SparkConf();
        conf.setAppName("SelectTop2NonUnique");
        conf.setMaster("local");

        JavaSparkContext context = new JavaSparkContext(conf);
        final int N = 2;

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("top.n.input.txt").getAbsolutePath());

        JavaRDD<String> rdd = lines.repartition(9);

        JavaPairRDD<String, Integer> kv = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] tokens = s.split(",");
                return new Tuple2<String, Integer>(tokens[0], Integer.parseInt(tokens[1]));
            }
        });

        JavaPairRDD<String, Integer> uniqueKeys = kv.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> finalTopN = uniqueKeys.takeOrdered(N, Comparator2.INSTANCE);

        for(int i=0; i < finalTopN.size(); ++i){
            Tuple2<String, Integer> t = finalTopN.get(i);
            System.out.println(t._1 + " = " + t._2);
        }

        context.close();
        //System.exit(0);
    }
    @Test
    public void testSelectTop2NonUnique(){
        SparkConf conf = new SparkConf();
        conf.setAppName("SelectTop2NonUnique");
        conf.setMaster("local");

        final JavaSparkContext context = new JavaSparkContext(conf);

        final int N = 2;

        final Broadcast<Integer> topN = context.broadcast(N);

        JavaRDD<String> lines = context.textFile("file://" + FileUtils.getResourceFile("top.n.input.txt").getAbsolutePath());

        int numPartitions = 9; // general rules: no. of partitions = no. of executors x no. of cores per executor x 2
        JavaRDD<String> rdd = lines.repartition(numPartitions);

        System.out.println("partitions: " + rdd.partitions().size());

        JavaPairRDD<String, Integer> kv = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] tokens = s.split(",");
                return new Tuple2<String, Integer>(tokens[0], Integer.parseInt(tokens[1]));
            }
        });

        JavaPairRDD<String, Integer> uniqueKeys = kv.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) throws Exception {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = uniqueKeys.collect();
        for(int i=0; i < output.size(); ++i){
            System.out.println(output.get(i)._1 + " = "+output.get(i)._2);
        }



        JavaRDD<SortedMap<Integer, String>> partitions = uniqueKeys.mapPartitions(new FlatMapFunction<Iterator<Tuple2<String, Integer>>, SortedMap<Integer, String>>() {
            public Iterable<SortedMap<Integer, String>> call(Iterator<Tuple2<String, Integer>> s) throws Exception {
                SortedMap<Integer, String> localTopN = new TreeMap<Integer, String>();
                final int N = topN.getValue();
                while (s.hasNext()) {
                    Tuple2<String, Integer> t = s.next();
                    localTopN.put(t._2, t._1);
                    if (localTopN.size() > N) {
                        localTopN.remove(localTopN.firstKey());
                    }
                }
                return Collections.singletonList(localTopN);
            }
        });



        SortedMap<Integer, String> finalTopN = new TreeMap<Integer, String>();
        List<SortedMap<Integer, String>> allTopN = partitions.collect();

        for(int i=0; i < allTopN.size(); ++i){
            SortedMap<Integer, String> localTopN = allTopN.get(i);
            System.out.println("-- Partition " + i + " --");
            System.out.println("-----------------");
            for(Map.Entry<Integer, String> entry : localTopN.entrySet()){
                System.out.println(entry.getValue() + " = " + entry.getKey());
                finalTopN.put(entry.getKey(), entry.getValue());
                if(finalTopN.size() > N){
                    System.out.println("Removing "+finalTopN.get(finalTopN.firstKey()) + " = " + finalTopN.firstKey());
                    finalTopN.remove(finalTopN.firstKey());
                }
            }
            System.out.println("-----------------");
        }

        System.out.println("---TOP N LIST---");
        System.out.println("----------------");

        for(Map.Entry<Integer, String> entry : finalTopN.entrySet()){
            System.out.println(entry.getValue()+" = "+entry.getKey());
        }

        System.out.println("----------------");


        context.close();
        //System.exit(0);
    }
}
