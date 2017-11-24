package com.github.chen0040.ml.tests.textmining.topicmodeling;

import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.tests.textmining.utils.FileUtils;
import com.github.chen0040.ml.textmining.topicmodeling.Lda;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.textmining.topicmodeling.LdaBatchUpdateResult;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/5/15.
 */
public class LdaTest {
    private static Lda method;
    private List<String> vocabulary;

    @BeforeClass
    public void setup(){

        method = new Lda();

        List<String> stopWords = new ArrayList<>();

        File file = FileUtils.getResourceFile("stoplist.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line=reader.readLine())!=null){
                String word = line.trim();
                if(!word.equals("")){
                    stopWords.add(word);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        method.addStopWords(stopWords);
    }

    private IntelliContext createBatch(){

        File file = FileUtils.getResourceFile("documents.txt");

        IntelliContext batch=new IntelliContext();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            reader.lines().forEach(line->{
                line = line.trim();
                if(line.equals("")) return;

                int docID = 0;
                String docDate = "";
                String[] fields = line.split("\t");
                String text = fields[0];  // Assume there's just one field, the text
                if (fields.length == 3) {  // If it's in [ID]\t[TAG]\t[TEXT] format...
                    //docID = Integer.parseInt(fields[0]);
                    //docDate = fields[1]; // do not interpret date as anything but a string
                    text = fields[2];
                }

                BasicDocument document = new BasicDocument();
                document.initialize(text);
                batch.addDoc(document);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return batch;
    }

    @Test
    public void testLDA(){

        IntelliContext batch = createBatch();

        method.setAttribute(Lda.TOPIC_COUNT, 20);
        method.setAttribute(Lda.MAX_VOCABULARY_SIZE, 10000);
        LdaBatchUpdateResult result = (LdaBatchUpdateResult)method.batchUpdate(batch);

        int topicCount = result.topicCount();

        System.out.println("Topic Count: "+topicCount);



       /*
        File modelFile = new File("myLDAModel");
        if(!modelFile.exists()){
            ldaModel.save(context.sc(), "myLDAModel");
            DistributedLDAModel sameModel = DistributedLDAModel.load(context.sc(), "myLDAModel");
        }
        */

    }
}
