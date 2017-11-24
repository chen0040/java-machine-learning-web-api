package com.github.chen0040.ml.tests.spark.basic;

import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by memeanalytics on 11/9/15.
 */
public class SecondarySortTest implements Serializable {



    @Test
    public void testOption1(){
        SparkConf sparkConf = new SparkConf().setAppName("SecondarySortOption1").setMaster("local");
        //sparkConf.set("spark.executor.memory","1g");
        //sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        final JavaSparkContext context = new JavaSparkContext(sparkConf);

        File file = FileUtils.getResourceFile("xyz.txt");

        System.out.println(file.getAbsolutePath());

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line = reader.readLine()) != null){
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JavaRDD<String> lines = context.textFile("file://" + file.getAbsolutePath());

        JavaPairRDD<String, Tuple2<Integer, Integer>> pairs = lines.mapToPair(new PairFunction<String, String, Tuple2<Integer, Integer>>() {
            public Tuple2<String, Tuple2<Integer, Integer>> call(String s) throws Exception {
                String[] tokens = s.split(",");
                System.out.println(tokens[0] + "," + tokens[1] + "," + tokens[2]);
                Integer time = Integer.parseInt(tokens[1]);
                Integer value = Integer.parseInt(tokens[2]);
                Tuple2<Integer, Integer> timeValue = new Tuple2<Integer, Integer>(time, value);
                return new Tuple2<String, Tuple2<Integer, Integer>>(tokens[0], timeValue);
            }
        });

        List<Tuple2<String, Tuple2<Integer, Integer>>> output = pairs.collect();

        for(Tuple2 t : output){
            Tuple2<Integer, Integer> timeValue = (Tuple2<Integer, Integer>)t._2;
            System.out.println(t._1 + ","+timeValue._1+","+timeValue._2);

        }

        JavaPairRDD<String, Iterable<Tuple2<Integer, Integer>>> groups = pairs.groupByKey();

        System.out.println("===DEBUG1===");
        List<Tuple2<String, Iterable<Tuple2<Integer, Integer>>>> output2 = groups.collect();
        for(Tuple2 t : output2){
            Iterable<Tuple2<Integer, Integer>> timeValues = (Iterable<Tuple2<Integer, Integer>>) t._2;
            System.out.println(t._1);
            for(Tuple2<Integer, Integer> t2 : timeValues){
                System.out.println(t2._1+","+t2._2);
            }
            System.out.println("=====");
        }

        JavaPairRDD<String, Iterable<Tuple2<Integer, Integer>>> sorted = groups.mapValues(new Function<Iterable<Tuple2<Integer, Integer>>, Iterable<Tuple2<Integer, Integer>>>() {
            public Iterable<Tuple2<Integer, Integer>> call(Iterable<Tuple2<Integer, Integer>> s) throws Exception {

                List<Tuple2<Integer, Integer>> values = new ArrayList<Tuple2<Integer, Integer>>();

                for(Tuple2<Integer, Integer> t : s){
                    values.add(t);
                }

                Collections.sort(values, new Comparator<Tuple2<Integer, Integer>>(){

                    public int compare(Tuple2<Integer, Integer> o1, Tuple2<Integer, Integer> o2) {
                        return Integer.compare(o1._1, o2._1);
                    }
                });

                return values;
            }
        });

        System.out.println("===DEBUG2===");
        List<Tuple2<String, Iterable<Tuple2<Integer, Integer>>>> output3 = sorted.collect();
        for(Tuple2 t : output3){
            Iterable<Tuple2<Integer, Integer>> timeValues = (Iterable<Tuple2<Integer, Integer>>) t._2;
            System.out.println(t._1);
            for(Tuple2<Integer, Integer> t2 : timeValues){
                System.out.println(t2._1+","+t2._2);
            }
            System.out.println("=====");
        }


        context.close();
        //System.exit(0);
    }
}
