package com.github.chen0040.ml.textmining.malurls;

import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.textmining.commons.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class UrlReader {
    private static <T> List<T> mapDocTo(IntelliContext batch, Function<BasicDocument, T> f){
        List<T> result = new ArrayList<>();
        int m = batch.docCount();
        for(int i=0; i < m; ++i){
            result.add(f.apply(batch.docAtIndex(i)));
        }

        return result;
    }

    public static List<String> tokenize(BasicDocument doc){
        List<String> result = new ArrayList<>();
        for(String content : doc.getRawContents()){
            StringTokenizer tokenizer = new StringTokenizer(content);
            while(tokenizer.hasMoreTokens()){
                result.add(tokenizer.nextToken());
            }
        }
        return result;
    }

    public static String getUrl(BasicDocument doc, Function<String, Boolean> urlParser){
        List<String> words = tokenize(doc);
        String urlAddress = words.get(0);
        if(urlParser==null) {

        }
        return urlAddress;
    }

    public static List<Tuple2<String, String>> getLabelledUrls(IntelliContext trainData, Function<String, Boolean> urlParser){
        List<Tuple2<String, String>> dataList = mapDocTo(trainData, (doc -> {

            String urlAddress = getUrl(doc, urlParser);
            String label = doc.getLabel();

            return new Tuple2<>(urlAddress, label);
        }));
        return dataList;
    }
}
