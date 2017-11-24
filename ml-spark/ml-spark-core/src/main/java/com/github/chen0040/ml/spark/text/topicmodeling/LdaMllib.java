package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import org.apache.spark.api.java.JavaPairRDD;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/25/15.
 */
public class LdaMllib extends AbstractSparkMLModule {

    private LDAModel ldaModel;
    public static final String TOPIC_COUNT = "K";

    public LdaMllib(){
        super();
        setAttribute(TOPIC_COUNT, 5);
    }


    public LDAModel getLdaModel(){
        return ldaModel;
    }

    private JavaRDD<Vector> convert(JavaRDD<SparkMLTuple> batch){
        JavaRDD<Vector> rdd = batch.map(new Function<SparkMLTuple, Vector>() {
            public Vector call(SparkMLTuple tuple) throws Exception {

                return vector(tuple);
            }
        });
        return rdd;
    }

    private Vector vector(SparkMLTuple tuple){
        List<Tuple2<Integer, Double>> values = new ArrayList<Tuple2<Integer, Double>>();
        for(Integer index : tuple.keySet()){
            values.add(new Tuple2<Integer, Double>(index, tuple.get(index)));
        }

        return Vectors.sparse(tuple.getDimension(), values);
    }

    @Override
    public Object clone() {
        LdaMllib clone = new LdaMllib();
        clone.copy(this);
        return clone;
    }

    @Override
    public void copy(SparkMLModule rhs){
        super.copy(rhs);

        LdaMllib rhs2 = (LdaMllib)rhs;
        ldaModel = rhs2.ldaModel;
    }

    public int topicCount(){
        return (int)getAttribute(TOPIC_COUNT);
    }

    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch){

        JavaRDD<Vector> parsedData = convert(batch);

        // Index documents with unique IDs
        JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
                new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
                    public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
                        return doc_id.swap();
                    }
                }
        ));
        corpus.cache();

        // Cluster the documents into three topics using LDA
        ldaModel = new LDA().setK(topicCount()).run(corpus);





        return new BatchUpdateResult();
    }


}
