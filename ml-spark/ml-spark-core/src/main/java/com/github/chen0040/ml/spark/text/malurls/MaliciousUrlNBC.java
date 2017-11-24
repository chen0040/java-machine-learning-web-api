package com.github.chen0040.ml.spark.text.malurls;


import com.github.chen0040.ml.spark.bayes.NBC;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.utils.parsers.UrlEvaluator;
import com.github.chen0040.ml.spark.core.*;
import com.github.chen0040.ml.spark.utils.UrlData;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class MaliciousUrlNBC extends AbstractSparkMLModule {

    private NBC method = new NBC();
    private UrlFilter urlParser = null;

    public CountRepository getModel(){
        return method.getModel();
    }
    
    public void loadUrlParser(UrlFilter urlParser) {
        this.urlParser = urlParser;
    }

    public boolean isMalicious(String urlAddress){
        UrlData url = new UrlData();
        url.setUrlAddress(urlAddress);
        UrlEvaluator.evaluate(url);

        return isMalicious(url);
    }

    public boolean isMalicious(UrlData url) {

        SparkMLTuple tuple = new SparkMLTuple();

        double[] attributes = url.attributeArray();

        for (int j = 0; j < attributes.length; ++j) {
            double attribute = attributes[j];
            tuple.put(j, attribute);
        }

        return method.predict(tuple).equals("1");
    }

    @Override
    public double getAttribute(String name) {
        return method.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, double value) {
        method.setAttribute(name, value);
    }

    @Override
    public Object clone() {
        MaliciousUrlNBC clone = new MaliciousUrlNBC();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        MaliciousUrlNBC rhs2 = (MaliciousUrlNBC)rhs;
        method = (NBC)rhs2.method.clone();
    }

    public BatchUpdateResult trainByLabelledUrlAddresses(JavaPairRDD<String, String> labelledUrls){
        JavaRDD<SparkMLTuple> batch2 = labelledUrls.map(tuple2 -> {
            String urlAddress = tuple2._1();
            String label = tuple2._2();

            UrlData url = new UrlData();
            url.setUrlAddress(urlAddress);
            UrlEvaluator.evaluate(url);

            SparkMLTuple tuple = new SparkMLTuple();

            double[] attributes = url.attributeArray();

            for (int j = 0; j < attributes.length; ++j) {
                double attribute = attributes[j];
                tuple.put(j, attribute);
            }

            tuple.setLabelOutput(label);

            return tuple;
        });

        return method.batchUpdate(batch2);
    }

    public BatchUpdateResult trainByLabelledUrlDataset(JavaRDD<UrlData> batch){

        JavaRDD<SparkMLTuple> batch2 = batch.map(url -> {

            SparkMLTuple tuple = new SparkMLTuple();

            double[] attributes = url.attributeArray();

            for (int j = 0; j < attributes.length; ++j) {
                double attribute = attributes[j];
                tuple.put(j, attribute);
            }

            tuple.setLabelOutput("" + url.getResult());

            return tuple;
        });

        return method.batchUpdate(batch2);
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch) {
        return method.batchUpdate(batch);
    }


}
