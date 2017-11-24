package com.github.chen0040.ml.spark.core.anomaly;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

/**
 * Created by memeanalytics on 13/8/15.
 */
public abstract class AbstractAnomalyDetecter extends AbstractSparkMLModule implements AnomalyDetector {

    public AbstractAnomalyDetecter(){
        super();
        setIsLabelRequiredInBatchUpdate(false);
    }

    public JavaRDD<SparkMLTuple> findOutliers(final JavaRDD<SparkMLTuple> batch){
        return batch.filter(new Function<SparkMLTuple, Boolean>() {
            public Boolean call(SparkMLTuple tuple) throws Exception {
                return isAnomaly(tuple);
            }
        });
    }


    public JavaRDD<String> findOutlierIds(JavaRDD<SparkMLTuple> batch){
        JavaRDD<SparkMLTuple> filtered = findOutliers(batch);

        JavaRDD<String> rdd = filtered.map(new Function<SparkMLTuple, String>() {
            public String call(SparkMLTuple tuple) throws Exception {
                return tuple.getId();
            }
        });

        return rdd;
    }

    public abstract boolean isAnomaly(SparkMLTuple tuple);
}
