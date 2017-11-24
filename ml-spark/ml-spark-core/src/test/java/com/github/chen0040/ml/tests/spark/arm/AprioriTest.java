package com.github.chen0040.ml.tests.spark.arm;

import com.github.chen0040.ml.spark.arm.Apriori;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.arm.ItemSetWarehouse;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import com.github.chen0040.ml.spark.core.arm.ItemSet;
import com.github.chen0040.ml.spark.core.arm.ItemSetCluster;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Created by memeanalytics on 13/9/15.
 */
public class AprioriTest extends MLearnTestCase {
    public AprioriTest(){
        super("Apriori");
    }

    private static Function<String, SparkMLTuple> parseLine = new Function<String, SparkMLTuple>() {
        public SparkMLTuple call(String s) throws Exception {
            String[] tokens = s.split(",");
            List<String> result = new ArrayList<>();
            for(String token : tokens){
                result.add(token.trim());
            }
            SparkMLTuple tuple = new SparkMLTuple();
            tuple.getDocBag().add(result);
            return tuple;
        }
    };

    @Test
    public void testGetUniqueItems(){


        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("arm.txt").getAbsolutePath());
        JavaRDD<SparkMLTuple> batch = lines.map(parseLine);

        Apriori method = new Apriori();
        List<String> items = method.scan4UniqueItems(batch);
        assertEquals(4, items.size());

        assertTrue(items.contains("a"));
        assertTrue(items.contains("b"));
        assertTrue(items.contains("c"));
        assertTrue(items.contains("d"));
    }

    @Test
    public void testApriori(){
        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("arm.txt").getAbsolutePath());
        JavaRDD<SparkMLTuple> batch = lines.map(parseLine);

        Apriori method = new Apriori();
        method.setMinSupportLevel(2);
        method.batchUpdate(batch);

        ItemSetWarehouse wh = method.getFrequentItemSets();

        for(int i=0; i < wh.size(); ++i){
            ItemSetCluster ic = wh.get(i);
            for(int j=0; j < ic.size(); ++j){
                ItemSet is = ic.get(j);
                System.out.println(is);
            }
        }
    }
}
