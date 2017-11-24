package com.github.chen0040.ml.tests.textmining;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.tests.textmining.utils.FileUtils;
import com.github.chen0040.ml.textmining.commons.es.BasicBatchJSONFactory;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Created by root on 9/16/15.
 */
public class ESJsonTest {
    @Test
    public void testJson(){

        try {
            IntelliContext batch = BasicBatchJSONFactory.getDefault().getBatch(new FileInputStream(FileUtils.getResourceFile("json_v0.txt")));
            int m = batch.tupleCount();

            for(int i=0; i < m; ++i){
                BasicDocument doc = batch.docAtIndex(i);
                HashMap<String, Integer> wordCounts = doc.getWordCounts();
                System.out.println("Doc: " + i);

                for(String word : wordCounts.keySet()){
                    System.out.println("Word: "+word+"\tCount: "+wordCounts.get(word));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
