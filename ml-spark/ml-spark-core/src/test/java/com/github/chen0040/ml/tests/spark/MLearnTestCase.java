package com.github.chen0040.ml.tests.spark;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by root on 9/14/15.
 */
public class MLearnTestCase implements Serializable {
    protected JavaSparkContext context;
    protected String appName;

    private static void turnOffSparkLogs() {
    }

    public MLearnTestCase(String appName){
        this.appName = appName;
    }

    @BeforeClass
    public void setup(){
        SparkConf config = new SparkConf();
        config.setAppName(appName);
        config.setMaster("local[*]");



        // set up a fast serializer
        //config.set("spark.serializer", "org.apache.spark.serializer.KrytoSerializer");
        //config.set("spark.krytoserializer.buffer.mb", "32");

        context = new JavaSparkContext(config);


        turnOffSparkLogs();
    }

    @AfterClass
    public void cleanup(){
        if(context != null) {
            context.stop();
            context.close();
        }
    }

    protected void show(JavaRDD<String> lines){
        List<String> data = lines.collect();
        for(int i=0; i < data.size(); ++i){
            System.out.println(data.get(i));
        }
    }

    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public JavaRDD<SparkMLTuple> readMessageFromESJson(){
        List<String> keywords = new ArrayList<>();
        keywords.add("error");
        keywords.add("systemd-logind");
        int timeWindowSize = 900000;

        JavaRDD<SparkMLTuple> batch = null;
        try {
            InputStream reader = new FileInputStream(FileUtils.getResourceFile("json_v0.txt"));
            batch = ESFactory.getDefault().readJsonOnMessage(context, reader, timeWindowSize, keywords);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return batch;
    }

    private static Function<String, SparkMLTuple> parseHeartScale = new Function<String, SparkMLTuple>() {
        public SparkMLTuple call(String line) throws Exception {
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            SparkMLTuple tuple = new SparkMLTuple();
            double output = atof(st.nextToken());
            tuple.setLabelOutput(output > 0 ? "Positive" : "Negative");

            int m = st.countTokens() / 2;
            for (int j = 0; j < m; j++) {

                int index = atoi(st.nextToken()) - 1;
                double value = atof(st.nextToken());

                tuple.put(index, value);
            }

            return tuple;
        }
    };

    public JavaRDD<SparkMLTuple> readHeartScale(){
        File file = FileUtils.getResourceFile("heart_scale");

        JavaRDD<String> lines = context.textFile(file.getAbsolutePath());

        JavaRDD<SparkMLTuple> rdd = lines.map(parseHeartScale);

        return rdd;

    }
}
