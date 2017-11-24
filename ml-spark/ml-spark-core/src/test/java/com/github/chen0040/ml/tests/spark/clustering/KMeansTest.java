package com.github.chen0040.ml.tests.spark.clustering;

import com.github.chen0040.ml.spark.clustering.KMeans;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import org.apache.spark.api.java.JavaRDD;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by root on 9/16/15.
 */
public class KMeansTest extends MLearnTestCase {
    public KMeansTest(){
        super("KMeans");
    }

    @Test
    public void testJson(){
        JavaRDD<SparkMLTuple> json_rdd = readMessageFromESJson();

        KMeans method = new KMeans();

        method.batchUpdate(json_rdd);

        List<SparkMLTuple> output = json_rdd.collect();
        for(int i=0; i < output.size(); ++i){
            int clusterLabel = method.getCluster(null, output.get(i));
            System.out.println("Cluster ID: "+clusterLabel);
        }
    }


}
