package com.github.chen0040.ml.tests.spark.recommender;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.recommender.CWBTIAB;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.util.List;

/**
 * Created by root on 9/14/15.
 */
public class CWBTIABTest extends MLearnTestCase {
    public CWBTIABTest(){
        super("CWBTIAB");
    }

    private static Function<String, SparkMLTuple> parser = new Function<String, SparkMLTuple>() {
        public SparkMLTuple call(String s) throws Exception {
            String[] tokens = s.split(" ");
            String userId = tokens[0];
            String s2 = tokens[1];
            String[] tokens2 = s2.split(",");
            SparkMLTuple transaction = new SparkMLTuple();
            transaction.setLabelOutput(userId);
            for (String item : tokens2) {
                transaction.add(item);
            }
            return transaction;
        }
    };

    private JavaRDD<SparkMLTuple> parse(JavaRDD<String> lines){
        JavaRDD<SparkMLTuple> transactions = lines.map(parser);
        return transactions;
    }

    private void print(JavaRDD<SparkMLTuple> transactions){
        List<SparkMLTuple> tuples = transactions.collect();
        for(int i=0; i < tuples.size(); ++i){
            SparkMLTuple tuple = tuples.get(i);
            System.out.print(tuple.getLabelOutput());
            System.out.print(": ");
            List<String> items = tuple.toBagOfWords();
            for(int j=0; j < items.size(); ++j){
                String item = items.get(j);
                if(j != 0){
                    System.out.print(", ");
                }
                System.out.print(item);
            }
            System.out.println();
        }
    }

    @Test
    public void testSimple(){
        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("CWBTIAB.txt").getAbsolutePath());
        show(lines);

        JavaRDD<SparkMLTuple> transactions = parse(lines);
        print(transactions);

        CWBTIAB method = new CWBTIAB();
        method.setCustomerId("user1");
        method.setItemId("item2");
        method.setTopN(4);

        method.batchUpdate(transactions);

        List<Tuple2<String, Integer>> boughtTogether = method.getTopNItems();

        for(int i=0; i < boughtTogether.size(); ++i){
            Tuple2<String, Integer> t = boughtTogether.get(i);
            System.out.println(t._1+" ("+t._2+")");
        }


    }


}
