package com.github.chen0040.ml.spark.text.malurls;

import com.github.chen0040.ml.spark.core.SparkMLTuple;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class UrlReader {


    public static JavaPairRDD<String, String> getLabelledUrls(JavaRDD<SparkMLTuple> trainData, UrlFilter urlParser){
        JavaSparkContext sc = JavaSparkContext.fromSparkContext(trainData.context());

        Broadcast<UrlFilter> urlFilter_bc = sc.broadcast(urlParser);

        JavaPairRDD<String, String> dataList = trainData.mapToPair(doc -> {

            UrlFilter filter = urlFilter_bc.value();

            List<String> result = new ArrayList<>();
            List<String> words = doc.toBagOfWords();
            for (String content : words) {
                StringTokenizer tokenizer = new StringTokenizer(content);
                boolean isUrlFound = false;
                while (tokenizer.hasMoreTokens()) {
                    String s = tokenizer.nextToken();
                    if (filter.apply(s)) {
                        result.add(s);
                        isUrlFound = true;
                        break;
                    }
                }
                if(isUrlFound) break;
            }

            String urlAddress = result.get(0);

            String label = doc.getLabelOutput();

            return new Tuple2<>(urlAddress, label);
        });
        return dataList;
    }
}
