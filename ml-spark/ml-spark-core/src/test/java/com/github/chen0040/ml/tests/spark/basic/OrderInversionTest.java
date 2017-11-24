package com.github.chen0040.ml.tests.spark.basic;

import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class OrderInversionTest implements Serializable {
    @Test
    public void testOI(){
        SparkConf conf = new SparkConf();
        conf.setMaster("local");
        conf.setAppName("OI");

        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("oi.txt").getAbsolutePath());

        final int neighborhoodSize = 2;
        final Broadcast<Integer> neighborhoodSizeBroadcast = context.broadcast(neighborhoodSize);

        JavaPairRDD<String, Tuple2<String, Integer>> rdd = lines.flatMapToPair(new PairFlatMapFunction<String, String, Tuple2<String, Integer>>() {
            public Iterable<Tuple2<String, Tuple2<String, Integer>>> call(String s) throws Exception {
                String[] tokens = s.split(" ");
                final int neighborhoodSize = neighborhoodSizeBroadcast.getValue();
                List<Tuple2<String, Tuple2<String, Integer>>> result = new ArrayList<Tuple2<String, Tuple2<String, Integer>>>();
                for (int i = 0; i < tokens.length; ++i) {
                    int start = (i - neighborhoodSize);
                    if (start < 0) start = 0;
                    int end = (i + neighborhoodSize);
                    if(end >= tokens.length) end=tokens.length-1;
                    String word_i = tokens[i];
                    for (int j = start; j <= end; ++j) {
                        if (i == j) continue;
                        String word_j = tokens[j];
                        result.add(new Tuple2<String, Tuple2<String, Integer>>(word_i, new Tuple2<String, Integer>(word_j, 1)));

                    }
                    result.add(new Tuple2<String, Tuple2<String, Integer>>(word_i, new Tuple2<String, Integer>("*", end-start)));
                }
                return result;
            }
        });

        List<Tuple2<String, Tuple2<String, Integer>>> output2 = rdd.collect();
        for(int i=0; i < output2.size(); ++i){
            Tuple2<String, Tuple2<String, Integer>> t2 = output2.get(i);
            System.out.println("("+t2._1+", "+t2._2._1+") : "+t2._2._2);
        }


        JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> rdd2 = rdd.groupByKey();

        JavaPairRDD<String, Map<String, Double>> rdd3 = rdd2.mapValues(new Function<Iterable<Tuple2<String, Integer>>, Map<String, Double>>() {
            public Map<String, Double> call(Iterable<Tuple2<String, Integer>> s) throws Exception {
                Map<String, Double> map = new HashMap<String, Double>();
                int total_count = 0;
                for(Tuple2<String, Integer> t : s){
                    String word_j = t._1;
                    if(word_j.equals("*")){
                        total_count+=t._2;
                    }else{
                        if(map.containsKey(word_j)){
                            map.put(word_j, map.get(word_j)+t._2);
                        }else{
                            map.put(word_j, (double)t._2);
                        }
                    }
                }

                for(String word_j : map.keySet()){
                    map.put(word_j, map.get(word_j) / total_count);
                }
                return map;
            }
        });

        List<Tuple2<String, Map<String, Double>>> output = rdd3.collect();

        for(int i=0; i < output.size(); ++i){
            String word_i = output.get(i)._1;
            Map<String, Double> t2 = output.get(i)._2;
            for(String word_j : t2.keySet()){
                System.out.println("("+word_i+", "+word_j+") : "+t2.get(word_j));
            }
        }

        context.close();



    }
}
