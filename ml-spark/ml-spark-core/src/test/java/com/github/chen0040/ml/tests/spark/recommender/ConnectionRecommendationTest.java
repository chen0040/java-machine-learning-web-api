package com.github.chen0040.ml.tests.spark.recommender;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.recommender.ConnectionRecommendation;
import com.github.chen0040.ml.spark.recommender.ConnectionRecommendationModel;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by memeanalytics on 14/9/15.
 */
public class ConnectionRecommendationTest extends MLearnTestCase {
    public ConnectionRecommendationTest() {
        super("ConnectionRecommendation");
    }

    private void print(JavaRDD<SparkMLTuple> friend_linkedin){
        List<SparkMLTuple> tuples = friend_linkedin.collect();
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

    @Test
    public void testSimple(){
        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("friends.txt").getAbsolutePath());
        show(lines);

        JavaRDD<SparkMLTuple> transactions = parse(lines);
        print(transactions);

        ConnectionRecommendation method = new ConnectionRecommendation();

        method.batchUpdate(transactions);

        List<ConnectionRecommendationModel> potential_connections = method.getModel();

        for(int i=0; i < potential_connections.size(); ++i){
            ConnectionRecommendationModel t = potential_connections.get(i);
            System.out.println(t.friend1 + " & " + t.friend2 + " should connect: they share " + t.commonFriends.size() + " common friends");
        }
    }
}
