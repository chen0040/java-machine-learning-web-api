package com.github.chen0040.ml.tests.spark.text.malurls;

import com.github.chen0040.ml.spark.text.malurls.MaliciousUrlNBC;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.spark.utils.UrlData;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.testng.annotations.Test;
import scala.Tuple2;

/**
 * Created by memeanalytics on 3/11/15.
 */
public class MaliciousUrlNBCTest extends MLearnTestCase {

    private static MaliciousUrlNBC method;

    public MaliciousUrlNBCTest(){
        super("Malicious URL NBC");
    }

    @Override
    public void setup(){
        super.setup();
        method = new MaliciousUrlNBC();
    }


    @Test
    public void testNBC(){

        JavaRDD<UrlData> batch = ARFFUtils.loadSample(context);

        method.trainByLabelledUrlDataset(batch);

        JavaPairRDD<Integer, Integer> successRDD = batch.mapToPair(url -> {
            boolean prediction = method.isMalicious(url);
            int predictedResult = prediction ? 1 : -1;
            int val1 = predictedResult==url.getResult()  ? 1 : 0;
            int val2 = 1;
            return new Tuple2<>(val1, val2);
        });

        Tuple2<Integer, Integer> counts = successRDD.reduce((i1, i2)->new Tuple2<>(i1._1()+i2._1(), i1._2() + i2._2()));

        double accuracy = (double)counts._1() / counts._2();

        System.out.println("Training Accuracy: " + accuracy);

        /*
        File modelFile = new File("myLDAModel");
        if(!modelFile.exists()){
            ldaModel.save(context.sc(), "myLDAModel");
            DistributedLDAModel sameModel = DistributedLDAModel.load(context.sc(), "myLDAModel");
        }
        */

    }





}
