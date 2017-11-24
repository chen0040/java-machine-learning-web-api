package com.github.chen0040.ml.tests.spark.basic;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class ElasticSearchTest {
    private JavaSparkContext context;

    @BeforeClass
    public void setup(){
        SparkConf config = new SparkConf();
        config.setAppName("apriori");
        config.setMaster("local");

        // set up a fast serializer
        //config.set("spark.serializer", "org.apache.spark.serializer.KrytoSerializer");
        //config.set("spark.krytoserializer.buffer.mb", "32");

        context = new JavaSparkContext(config);
    }

    @AfterClass
    public void cleanup(){
        context.close();
    }

    @Test
    public void testReadJson(){
        List<String> keywords = new ArrayList<>();
        keywords.add("error");
        keywords.add("systemd-logind");
        int timeWindowSize = 900000;

        try {
            InputStream reader = new FileInputStream(FileUtils.getResourceFile("json_v0.txt"));
            JavaRDD<SparkMLTuple> batch = ESFactory.getDefault().readJsonOnMessage(context, reader, timeWindowSize, keywords);

            List<SparkMLTuple> tuples = batch.collect();
            for(int i=0; i < tuples.size(); ++i){
                SparkMLTuple tuple = tuples.get(i);
                StringBuilder sb = new StringBuilder();
                for(int j=0; j < tuple.getDimension(); ++j){
                    double value = tuple.getOrDefault(j, 0.0);
                    if(j != 0) sb.append(", ");
                    sb.append(value);
                }
                System.out.println(sb.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
