package com.github.chen0040.ml.tests.spark.basic;

import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import com.google.common.base.Optional;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created by memeanalytics on 12/9/15.
 */
public class LeftOuterJoinTest implements Serializable {
    @Test
    public void testLeftJoin(){
        SparkConf conf = new SparkConf();
        conf.setAppName("LeftJoin");
        conf.setMaster("local");

        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("join.users.txt").getAbsolutePath());
        JavaPairRDD<String, Tuple2<String, String>> users = lines.mapToPair(new PairFunction<String, String, Tuple2<String, String>>(){
            public Tuple2<String, Tuple2<String, String>> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String userId = tokens[0];
                String locationId = tokens[1];
                return new Tuple2<String, Tuple2<String, String>>(userId, new Tuple2<String, String>("L", locationId));
            }
        });

        lines = context.textFile(FileUtils.getResourceFile("join.transactions.txt").getAbsolutePath());
        JavaPairRDD<String, Tuple2<String, String>> transactions = lines.mapToPair(new PairFunction<String, String, Tuple2<String, String>>() {
            public Tuple2<String, Tuple2<String, String>> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String productId = tokens[1];
                String userId = tokens[2];
                return new Tuple2<String, Tuple2<String, String>>(userId, new Tuple2<String, String>("P", productId));
            }
        });

        JavaPairRDD<String, Tuple2<String, String>> user_transactions= users.union(transactions);

        JavaPairRDD<String, Iterable<Tuple2<String, String>>> grouped_by_user = user_transactions.groupByKey();

        JavaPairRDD<String, String> product_locations = grouped_by_user.flatMapToPair(new PairFlatMapFunction<Tuple2<String, Iterable<Tuple2<String, String>>>, String, String>() {
            public Iterable<Tuple2<String, String>> call(Tuple2<String, Iterable<Tuple2<String, String>>> s) throws Exception {
                List<String> products = new ArrayList<>();
                String location = "UNKNOWN";
                for (Tuple2<String, String> t : s._2) {
                    if (t._1.equals("L")) {
                        location = t._2;
                    } else {
                        products.add(t._2);
                    }
                }
                List<Tuple2<String, String>> product_locations = new ArrayList<Tuple2<String, String>>();
                for (String product : products) {
                    product_locations.add(new Tuple2<String, String>(product, location));
                }
                return product_locations;
            }
        });

        JavaPairRDD<String, Iterable<String>> locationsByProduct = product_locations.groupByKey();

        List<Tuple2<String, Iterable<String>>> output = locationsByProduct.collect();

        for(Tuple2<String, Iterable<String>> t : output){
            String product = t._1;
            Iterable<String> locations = t._2;
            for(String location : locations){
                System.out.println(product + " : " + location);
            }
        }



        context.close();
        //System.exit(0);
    }

    @Test
    public void testLeftOuterJoin(){
        SparkConf conf = new SparkConf();
        conf.setAppName("LeftOuterJoin");
        conf.setMaster("local");

        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("join.transactions.txt").getAbsolutePath());
        JavaPairRDD<String, String> transactions = lines.mapToPair(new PairFunction<String, String, String>() {
            public Tuple2<String, String> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String productId = tokens[1];
                String userId = tokens[2];
                return new Tuple2<String, String>(userId, productId);
            }
        });
        lines = context.textFile(FileUtils.getResourceFile("join.users.txt").getAbsolutePath());
        JavaPairRDD<String, String> users = lines.mapToPair(new PairFunction<String, String, String>() {
            public Tuple2<String, String> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String userId = tokens[0];
                String locationId = tokens[1];
                return new Tuple2<String, String>(userId, locationId);
            }
        });

        JavaPairRDD<String, Tuple2<String, Optional<String>>> joined = transactions.leftOuterJoin(users);

        JavaPairRDD<String, String> product_locations  = joined.mapToPair(new PairFunction<Tuple2<String, Tuple2<String, Optional<String>>>, String, String>() {
            public Tuple2<String, String> call(Tuple2<String, Tuple2<String, Optional<String>>> s) throws Exception {
                String userId = s._1;
                Tuple2<String, Optional<String>> product_location = s._2;
                return new Tuple2<String, String>(product_location._1, product_location._2.get());
            }
        });

        JavaPairRDD<String, Iterable<String>> locationsByProduct = product_locations.groupByKey();

        List<Tuple2<String, Iterable<String>>> output = locationsByProduct.collect();

        for(Tuple2<String, Iterable<String>> t : output){
            String productId = t._1;
            Iterable<String> locations = t._2;
            for(String t2 : locations){
                System.out.println(productId + " : "+t2);
            }
        }

        context.close();
        //System.exit(0);

    }

    @Test
    public void testCombineByKey(){
        SparkConf conf = new SparkConf();
        conf.setAppName("LeftJoin");
        conf.setMaster("local");

        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("join.transactions.txt").getAbsolutePath());
        JavaPairRDD<String, String> transactions = lines.mapToPair(new PairFunction<String, String, String>() {
            public Tuple2<String, String> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String product = tokens[1];
                String user = tokens[2];
                return new Tuple2<String, String>(user, product);
            }
        });

        lines = context.textFile(FileUtils.getResourceFile("join.users.txt").getAbsolutePath());
        JavaPairRDD<String, String> users = lines.mapToPair(new PairFunction<String, String, String>() {
            public Tuple2<String, String> call(String s) throws Exception {
                String[] tokens = s.split(",");
                String user = tokens[0];
                String location = tokens[1];
                return new Tuple2<String, String>(user, location);
            }
        });

        JavaPairRDD<String, Tuple2<String, Optional<String>>> joined = transactions.leftOuterJoin(users);

        JavaPairRDD<String, String> product_locations = joined.mapToPair(new PairFunction<Tuple2<String, Tuple2<String, Optional<String>>>, String, String>() {
            public Tuple2<String, String> call(Tuple2<String, Tuple2<String, Optional<String>>> s) throws Exception {
                return new Tuple2<String, String>(s._2._1, s._2._2.get());
            }
        });



        JavaPairRDD<String, Set<String>> combined = product_locations.combineByKey(new Function<String, Set<String>>() {
            public Set<String> call(String s) throws Exception {
                Set<String> set = new HashSet<>();
                set.add(s);
                return set;
            }
        }, new Function2<Set<String>, String, Set<String>>() {
            public Set<String> call(Set<String> set, String s) throws Exception {
                set.add(s);
                return set;
            }
        }, new Function2<Set<String>, Set<String>, Set<String>>() {
            public Set<String> call(Set<String> set1, Set<String> set2) throws Exception {
                set1.addAll(set2);
                return set1;
            }
        });

        Map<String, Set<String>> output = combined.collectAsMap();

        for(String product : output.keySet()){
            Set<String> locations = output.get(product);
            for(String location : locations){
                System.out.println(product + " = " + location);
            }
        }

        context.close();
        //System.exit(0);

    }


}
