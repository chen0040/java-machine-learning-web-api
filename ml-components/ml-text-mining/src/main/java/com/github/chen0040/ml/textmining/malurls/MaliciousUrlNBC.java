package com.github.chen0040.ml.textmining.malurls;

import com.github.chen0040.ml.bayes.nbc.NBC;
import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.textmining.commons.Tuple2;
import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.textmining.commons.UrlData;
import com.github.chen0040.ml.textmining.commons.parsers.UrlEvaluator;

import java.util.List;
import java.util.function.Function;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class MaliciousUrlNBC extends AbstractMLModule {

    private NBC method = new NBC();
    private Function<String, Boolean> urlParser = null;

    public void loadUrlParser(Function<String, Boolean> urlParser) {
        this.urlParser = urlParser;
    }

    public boolean isMalicious(String urlAddress){
        UrlData url = new UrlData();
        url.setUrlAddress(urlAddress);
        UrlEvaluator.evaluate(url);

        return isMalicious(url);
    }

    public boolean isMalicious(UrlData url) {

        IntelliTuple tuple = method.newTuple();

        double[] attributes = url.attributeArray();

        for (int j = 0; j < attributes.length; ++j) {
            double attribute = attributes[j];
            tuple.set(j, attribute);
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
    public void copy(MLModule rhs){
        super.copy(rhs);

        MaliciousUrlNBC rhs2 = (MaliciousUrlNBC)rhs;
        method = (NBC)rhs2.method.clone();
    }

    public BatchUpdateResult trainByLabelledUrlDataSet(List<UrlData> data){
        IntelliContext batch2 = new IntelliContext();
        int m = data.size();
        for(int i=0; i < m; ++i) {

            UrlData url = data.get(i);

            IntelliTuple tuple = batch2.newTuple();

            double[] attributes = url.attributeArray();

            for (int j = 0; j < attributes.length; ++j) {
                double attribute = attributes[j];
                tuple.set(j, attribute);
                batch2.getAttributeLevelSource().setAttributeName(j, "c" + j);
            }

            tuple.setLabelOutput("" + url.getResult());

            batch2.add(tuple);
        }

        return method.batchUpdate(batch2);
    }

    public BatchUpdateResult trainByLabelledUrls(List<Tuple2<String, String>> labelledUrls){
        IntelliContext batch2 = new IntelliContext();
        int m = labelledUrls.size();
        for(int i=0; i < m; ++i) {
            Tuple2<String, String> tuple2 = labelledUrls.get(i);
            String urlAddress = tuple2._1();
            String label = tuple2._2();

            UrlData url = new UrlData();
            url.setUrlAddress(urlAddress);
            UrlEvaluator.evaluate(url);

            IntelliTuple tuple = batch2.newTuple();

            double[] attributes = url.attributeArray();

            for (int j = 0; j < attributes.length; ++j) {
                double attribute = attributes[j];
                tuple.set(j, attribute);
                batch2.getAttributeLevelSource().setAttributeName(j, "c" + j);
            }

            tuple.setLabelOutput(label);

            batch2.add(tuple);
        }

        return method.batchUpdate(batch2);
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        return method.batchUpdate(batch);
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        return method.evaluate(tuple, context);
    }
}
