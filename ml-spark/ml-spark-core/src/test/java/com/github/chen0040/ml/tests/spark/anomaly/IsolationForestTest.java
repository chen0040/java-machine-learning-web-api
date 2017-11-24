package com.github.chen0040.ml.tests.spark.anomaly;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.trees.iforest.IsolationForest;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 13/9/15.
 */
public class IsolationForestTest extends MLearnTestCase {

    private static JavaRDD<SparkMLTuple> batch;
    private static JavaRDD<SparkMLTuple> batchBySeverity;
    private static IsolationForest iforest;

    public IsolationForestTest(){
        super("IsolationForest");
    }

    @Override
    public void setup(){
        super.setup();
        batch = readMessageFromESJson();
        batchBySeverity = readSeverityFromESJson();
        iforest = new IsolationForest();
    }

    public JavaRDD<SparkMLTuple> readSeverityFromESJson(){
        List<String> severitiesToTrack = new ArrayList<>();

        severitiesToTrack.add("ERROR");
        severitiesToTrack.add("WARNING");
        int timeWindowSize = 900000;

        JavaRDD<SparkMLTuple> batch = null;
        try {
            InputStream reader = new FileInputStream(FileUtils.getResourceFile("json_v0.txt"));
            batch = ESFactory.getDefault().readJsonOnSeverity(context, reader, timeWindowSize, severitiesToTrack);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return batch;
    }

    private static PairFunction<SparkMLTuple, SparkMLTuple, Boolean> checkAnomaly = new PairFunction<SparkMLTuple, SparkMLTuple, Boolean>() {
        public Tuple2<SparkMLTuple, Boolean> call(SparkMLTuple tuple) throws Exception {
            boolean anomaly = iforest.isAnomaly(tuple);
            return new Tuple2<SparkMLTuple, Boolean>(tuple, anomaly);
        }
    };

    @Test
    public void testAnomalyByMessage(){

        iforest.batchUpdate(batch);



        JavaPairRDD<SparkMLTuple, Boolean> result = batch.mapToPair(checkAnomaly);

        List<Tuple2<SparkMLTuple, Boolean>> output = result.collect();

        for(int i=0; i < output.size(); ++i){
            String label = output.get(i)._2() ? "+1" : "-1";
            SparkMLTuple tuple =  output.get(i)._1();

            StringBuilder sb = new StringBuilder();
            for(int j=0; j < tuple.getDimension(); ++j){
                if(j != 0){
                    sb.append(",\t");
                }
                sb.append(tuple.getOrDefault(j, Double.NaN));
            }

            System.out.println(label + "\t" + sb.toString());
        }
    }

    @Test
    public void testAnomalyBySeverity(){
        iforest.batchUpdate(batchBySeverity);
        //iforest.setAttribute(IsolationForest.THRESHOLD, 0.7);

        JavaPairRDD<SparkMLTuple, Boolean> result = batchBySeverity.mapToPair(checkAnomaly);

        List<Tuple2<SparkMLTuple, Boolean>> output = result.collect();

        for(int i=0; i < output.size(); ++i){
            String label = output.get(i)._2() ? "+1" : "-1";
            SparkMLTuple tuple =  output.get(i)._1();

            StringBuilder sb = new StringBuilder();
            for(int j=0; j < tuple.getDimension(); ++j){
                if(j != 0){
                    sb.append(",\t");
                }
                sb.append(tuple.getOrDefault(j, Double.NaN));
            }

            System.out.println(label + "\t" + sb.toString());
        }
    }


}
